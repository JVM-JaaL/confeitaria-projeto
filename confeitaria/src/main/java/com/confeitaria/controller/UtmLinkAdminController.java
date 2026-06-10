package com.confeitaria.controller;

import com.confeitaria.model.UtmLink;
import com.confeitaria.repository.UtmLinkRepository;
import com.confeitaria.util.UtmLinkUrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

// Gerencia os links de marketing com parâmetros UTM no painel admin.
// A URL final com todos os UTMs é montada por UtmLinkUrlBuilder.generateFullUrl() — não há
// redirecionamento automático; o link gerado é copiado e usado manualmente nas redes sociais.
// Depende de: UtmLinkRepository (persistência), UtmLinkUrlBuilder (geração da URL)
@Slf4j
@Controller
@RequestMapping("/admin/links-utm")
public class UtmLinkAdminController {

    private final UtmLinkRepository utmLinkRepo;

    public UtmLinkAdminController(UtmLinkRepository utmLinkRepo) {
        this.utmLinkRepo = utmLinkRepo;
    }

    // GET /admin/links-utm — lista todos os links do mais recente ao mais antigo
    @GetMapping
    public String list(Model model) {
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Links UTM");
        model.addAttribute("links", utmLinkRepo.findAllByOrderByCreatedAtDesc());
        return "admin/utm-links";
    }

    // GET /admin/links-utm/novo — exibe formulário em branco para criar novo link
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Novo link UTM");
        model.addAttribute("utmLink", new UtmLink());
        model.addAttribute("editMode", false); // template usa editMode para ajustar label do botão
        return "admin/utm-link-form";
    }

    // POST /admin/links-utm/novo — salva o novo link UTM
    @PostMapping("/novo")
    public String criar(@ModelAttribute UtmLink form) {
        form.setCreatedAt(Instant.now());
        utmLinkRepo.save(form);
        log.info("Link UTM criado: {}", form.getName());
        return "redirect:/admin/links-utm";
    }

    // GET /admin/links-utm/editar/{id} — exibe formulário preenchido com os dados do link existente
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        UtmLink link = utmLinkRepo.findById(id).orElseThrow();
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Editar link UTM");
        model.addAttribute("utmLink", link);
        model.addAttribute("editMode", true);
        return "admin/utm-link-form";
    }

    // POST /admin/links-utm/editar/{id} — aplica as alterações ao link existente
    @PostMapping("/editar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute UtmLink form) {
        UtmLink existing = utmLinkRepo.findById(id).orElseThrow();
        // Atualiza apenas os campos editáveis, preservando createdAt
        existing.setName(form.getName());
        existing.setBaseUrl(form.getBaseUrl());
        existing.setUtmSource(form.getUtmSource());
        existing.setUtmMedium(form.getUtmMedium());
        existing.setUtmCampaign(form.getUtmCampaign());
        existing.setUtmTerm(form.getUtmTerm());
        existing.setUtmContent(form.getUtmContent());
        existing.setShortDescription(form.getShortDescription());
        existing.setLastUpdated(Instant.now());
        utmLinkRepo.save(existing);
        log.info("Link UTM atualizado: {}", id);
        return "redirect:/admin/links-utm";
    }

    // POST /admin/links-utm/excluir/{id} — remove o link permanentemente
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        utmLinkRepo.deleteById(id);
        return "redirect:/admin/links-utm";
    }

    // GET /admin/links-utm/visualizar/{id} — exibe a URL completa gerada por UtmLinkUrlBuilder para copiar
    @GetMapping("/visualizar/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        UtmLink link = utmLinkRepo.findById(id).orElseThrow();
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Link UTM gerado");
        model.addAttribute("utmLink", link);
        model.addAttribute("fullUrl", UtmLinkUrlBuilder.generateFullUrl(link)); // URL final para copiar
        return "admin/utm-link-detalhe";
    }
}
