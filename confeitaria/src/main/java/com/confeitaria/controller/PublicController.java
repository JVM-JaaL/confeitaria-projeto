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

    @GetMapping("/")
    public String index(@RequestParam(required = false) String ref, Model model,
                        HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("navActive", "inicio");

        Object refAttr = model.asMap().get("ref");
        if (refAttr instanceof String rs && !rs.isBlank()) {
            referralRepo.findByCode(rs.toUpperCase()).ifPresent(link -> {
                link.setVisits(link.getVisits() + 1);
                referralRepo.save(link);
            });
        }
        return "public/index";
    }

    @GetMapping("/galeria")
    public String galeria(@RequestParam(required = false) String ref, Model model,
                          HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("gallery", galleryRepo.findByVisibleTrueOrderByDisplayOrderAsc());
        model.addAttribute("navActive", "galeria");
        return "public/galeria";
    }

    @GetMapping("/depoimentos")
    public String depoimentos(@RequestParam(required = false) String ref, Model model,
                              HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("testimonials", testimonialRepo.findByVisibleTrueOrderByCreatedAtDesc());
        model.addAttribute("navActive", "depoimentos");
        return "public/depoimentos";
    }

    @GetMapping({"/perguntas-frequentes", "/faq"})
    public String faq(@RequestParam(required = false) String ref, Model model,
                      HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("faqs", faqRepo.findByVisibleTrueOrderByDisplayOrderAsc());
        model.addAttribute("navActive", "faq");
        return "public/faq";
    }

    @GetMapping("/contato")
    public String contato(@RequestParam(required = false) String ref, Model model,
                          HttpServletRequest request, HttpSession session) {
        marketingService.enrichPublicModel(model, request, session, ref);
        model.addAttribute("contact", new Contact());
        model.addAttribute("navActive", "contato");
        return "public/contato";
    }

    @PostMapping("/contato")
    public String submitContato(@ModelAttribute Contact contact,
                                @RequestParam(required = false) String ref,
                                @RequestParam(required = false) String source,
                                HttpSession session) {
        contact.setCreatedAt(LocalDateTime.now());
        contact.setSource(source != null ? source : "site");
        marketingService.applyStoredMarketingToContact(contact, session);

        String effectiveRef = ref;
        if (effectiveRef == null || effectiveRef.isBlank()) {
            Object r = session.getAttribute(PublicMarketingService.SK_REF);
            if (r instanceof String rs && !rs.isBlank()) {
                effectiveRef = rs;
            }
        }

        if (effectiveRef != null && !effectiveRef.isBlank()) {
            contact.setReferralCode(effectiveRef.toUpperCase());
            referralRepo.findByCode(effectiveRef.toUpperCase()).ifPresent(link -> {
                link.setConversions(link.getConversions() + 1);
                referralRepo.save(link);
            });
        }

        contactRepo.save(contact);
        log.info("Contato enviado: ref={}, source={}", effectiveRef, contact.getSource());
        return marketingService.redirectWithMarketing("/contato", session, Map.of("sucesso", "true"));
    }
}
