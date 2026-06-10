package com.confeitaria.controller;

import com.confeitaria.model.Contact;
import com.confeitaria.repository.*;
import com.confeitaria.web.PublicMarketingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

// Controller de todas as páginas públicas do site (visitantes não autenticados).
// Em cada GET: chama marketingService.enrichPublicModel() para injetar o contexto UTM/ref no model.
// No POST de contato: salva o contato com rastreamento completo e redireciona para o WhatsApp.
// Depende de: PublicMarketingService (rastreamento), repositórios de conteúdo (galeria, depoimentos, FAQs)
@Slf4j
@Controller
public class PublicController {

    private final GalleryItemRepository galleryRepo;
    private final TestimonialRepository testimonialRepo;
    private final FaqRepository faqRepo;
    private final ContactRepository contactRepo;
    private final ReferralLinkRepository referralRepo;
    private final PublicMarketingService marketingService;

    public PublicController(GalleryItemRepository galleryRepo, TestimonialRepository testimonialRepo,
                            FaqRepository faqRepo, ContactRepository contactRepo,
                            ReferralLinkRepository referralRepo, PublicMarketingService marketingService) {
        this.galleryRepo = galleryRepo;
        this.testimonialRepo = testimonialRepo;
        this.faqRepo = faqRepo;
        this.contactRepo = contactRepo;
        this.referralRepo = referralRepo;
        this.marketingService = marketingService;
    }

    // GET / — página inicial. Se vier com ?ref=CODIGO, incrementa visitas no ReferralLink correspondente.
    @GetMapping("/")
    public String index(@RequestParam(required = false) String ref, Model model,
                        HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("navActive", "inicio");

        // Incrementa visitas do link de indicação se o visitante chegou via ?ref=
        Object refAttr = model.asMap().get("ref");
        if (refAttr instanceof String rs && !rs.isBlank()) {
            referralRepo.findByCode(rs.toUpperCase()).ifPresent(link -> {
                link.setVisits(link.getVisits() + 1);
                referralRepo.save(link);
            });
        }
        return "public/index";
    }

    // GET /galeria — exibe somente fotos visíveis, na ordem definida pelo admin
    @GetMapping("/galeria")
    public String galeria(@RequestParam(required = false) String ref, Model model,
                          HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("gallery", galleryRepo.findByVisibleTrueOrderByDisplayOrderAsc());
        model.addAttribute("navActive", "galeria");
        return "public/galeria";
    }

    // GET /depoimentos — exibe somente depoimentos visíveis, do mais recente ao mais antigo
    @GetMapping("/depoimentos")
    public String depoimentos(@RequestParam(required = false) String ref, Model model,
                              HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("testimonials", testimonialRepo.findByVisibleTrueOrderByCreatedAtDesc());
        model.addAttribute("navActive", "depoimentos");
        return "public/depoimentos";
    }

    // GET /perguntas-frequentes ou /faq — duas URLs apontam para a mesma página
    @GetMapping({"/perguntas-frequentes", "/faq"})
    public String faq(@RequestParam(required = false) String ref, Model model,
                      HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("faqs", faqRepo.findByVisibleTrueOrderByDisplayOrderAsc());
        model.addAttribute("navActive", "faq");
        return "public/faq";
    }

    // GET /contato — exibe o formulário de contato com objeto Contact vazio para binding
    @GetMapping("/contato")
    public String contato(@RequestParam(required = false) String ref, Model model,
                          HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("contact", new Contact());
        model.addAttribute("navActive", "contato");
        return "public/contato";
    }

    // POST /contato — salva o contato com UTMs da sessão, incrementa conversão do referral e redireciona para WhatsApp
    @PostMapping("/contato")
    public String submitContato(@ModelAttribute Contact contact,
                                @RequestParam(required = false) String ref,
                                @RequestParam(required = false) String source,
                                HttpSession session) {
        contact.setCreatedAt(LocalDateTime.now());
        contact.setSource(source != null ? source : "site");

        // Copia UTMs da sessão para o objeto Contact antes de salvar
        marketingService.applyStoredMarketingToContact(contact, session);

        // Prioriza o ref da URL; se não vier, usa o que está na sessão (visitante que navega)
        String effectiveRef = ref;
        if (effectiveRef == null || effectiveRef.isBlank()) {
            Object r = session.getAttribute(PublicMarketingService.SK_REF);
            if (r instanceof String rs && !rs.isBlank()) {
                effectiveRef = rs;
            }
        }

        // Salva o referral no contato e incrementa o contador de conversões
        if (effectiveRef != null && !effectiveRef.isBlank()) {
            contact.setReferralCode(effectiveRef.toUpperCase());
            referralRepo.findByCode(effectiveRef.toUpperCase()).ifPresent(link -> {
                link.setConversions(link.getConversions() + 1);
                referralRepo.save(link);
            });
        }

        contactRepo.save(contact);
        log.info("Contato enviado: ref={}, source={}", effectiveRef, contact.getSource());
        // Redireciona para o link externo de atendimento (WhatsApp)
        return "redirect:https://w.app/68oeeg";
    }
}
