package com.confeitaria.controller;

import com.confeitaria.model.UtmLink;
import com.confeitaria.repository.UtmLinkRepository;
import com.confeitaria.util.UtmLinkUrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@Controller
@RequestMapping("/admin/links-utm")
public class UtmLinkAdminController {

    private final UtmLinkRepository utmLinkRepo;

    public UtmLinkAdminController(UtmLinkRepository utmLinkRepo) {
        this.utmLinkRepo = utmLinkRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Links UTM");
        model.addAttribute("links", utmLinkRepo.findAllByOrderByCreatedAtDesc());
        return "admin/utm-links";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Novo link UTM");
        model.addAttribute("utmLink", new UtmLink());
        model.addAttribute("editMode", false);
        return "admin/utm-link-form";
    }

    @PostMapping("/novo")
    public String criar(@ModelAttribute UtmLink form) {
        form.setCreatedAt(Instant.now());
        utmLinkRepo.save(form);
        log.info("Link UTM criado: {}", form.getName());
        return "redirect:/admin/links-utm";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        UtmLink link = utmLinkRepo.findById(id).orElseThrow();
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Editar link UTM");
        model.addAttribute("utmLink", link);
        model.addAttribute("editMode", true);
        return "admin/utm-link-form";
    }

    @PostMapping("/editar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute UtmLink form) {
        UtmLink existing = utmLinkRepo.findById(id).orElseThrow();
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

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        utmLinkRepo.deleteById(id);
        return "redirect:/admin/links-utm";
    }

    @GetMapping("/visualizar/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        UtmLink link = utmLinkRepo.findById(id).orElseThrow();
        model.addAttribute("currentPage", "links-utm");
        model.addAttribute("pageTitle", "Link UTM gerado");
        model.addAttribute("utmLink", link);
        model.addAttribute("fullUrl", UtmLinkUrlBuilder.generateFullUrl(link));
        return "admin/utm-link-detalhe";
    }
}
