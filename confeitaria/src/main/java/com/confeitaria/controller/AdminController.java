package com.confeitaria.controller;

import com.confeitaria.model.ReferralLink;
import com.confeitaria.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContactRepository contactRepo;
    private final ReferralLinkRepository referralRepo;
    private final SaleRepository saleRepo;
    private final RecipeRepository recipeRepo;

    public AdminController(ContactRepository contactRepo, ReferralLinkRepository referralRepo,
                           SaleRepository saleRepo, RecipeRepository recipeRepo) {
        this.contactRepo = contactRepo;
        this.referralRepo = referralRepo;
        this.saleRepo = saleRepo;
        this.recipeRepo = recipeRepo;
    }

    @GetMapping("/login")
    public String login() { return "admin/login"; }

    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("totalContacts", contactRepo.count());
        model.addAttribute("totalSales", saleRepo.count());
        model.addAttribute("totalRecipes", recipeRepo.count());
        model.addAttribute("recentContacts", contactRepo.findAllByOrderByCreatedAtDesc().stream().limit(5).toList());
        model.addAttribute("sourceCounts", contactRepo.countBySource());
        model.addAttribute("referralCounts", contactRepo.countByReferralCode());
        model.addAttribute("referralLinks", referralRepo.findAll());
        log.info("Dashboard carregado: {} contatos, {} vendas", contactRepo.count(), saleRepo.count());
        return "admin/dashboard";
    }

    // ---- CONTACTS ----
    @GetMapping("/contatos")
    public String contatos(Model model) {
        model.addAttribute("contacts", contactRepo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("sourceCounts", contactRepo.countBySource());
        model.addAttribute("referralCounts", contactRepo.countByReferralCode());
        return "admin/contatos";
    }

    @PostMapping("/contatos/delete/{id}")
    public String deleteContato(@PathVariable Long id) {
        contactRepo.deleteById(id);
        return "redirect:/admin/contatos";
    }

    // ---- REFERRAL LINKS ----
    @PostMapping("/referral/add")
    public String addReferral(@RequestParam String code, @RequestParam String referrerName) {
        var ref = new ReferralLink();
        ref.setCode(code.toUpperCase());
        ref.setReferrerName(referrerName);
        referralRepo.save(ref);
        log.info("Referral adicionado: {}", code.toUpperCase());
        return "redirect:/admin";
    }

    @PostMapping("/referral/delete/{id}")
    public String deleteReferral(@PathVariable Long id) {
        referralRepo.deleteById(id);
        return "redirect:/admin";
    }
}
