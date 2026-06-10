package com.confeitaria.controller;

import com.confeitaria.model.ReferralLink;
import com.confeitaria.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Controller do núcleo do painel admin: login, dashboard e gerenciamento de contatos e referrals.
// O conteúdo editável (galeria, depoimentos, FAQs) foi separado em ContentAdminController.
// O módulo financeiro fica em SaleController, RecipeController, MonthlyExpenseController e FinancialDashboardController.
// Todas as rotas aqui exigem autenticação ADMIN (configurado em SecurityConfig).
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

    // GET /admin/login — exibe o formulário de login (processado pelo Spring Security em POST /admin/login)
    @GetMapping("/login")
    public String login() { return "admin/login"; }

    // GET /admin — painel principal com contagem de contatos/vendas/receitas, lista recente e gráficos de origem
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("totalContacts", contactRepo.count());
        model.addAttribute("totalSales", saleRepo.count());
        model.addAttribute("totalRecipes", recipeRepo.count());
        model.addAttribute("recentContacts", contactRepo.findAllByOrderByCreatedAtDesc().stream().limit(5).toList());
        model.addAttribute("sourceCounts", contactRepo.countBySource());       // gráfico de canais
        model.addAttribute("referralCounts", contactRepo.countByReferralCode()); // gráfico de indicações
        model.addAttribute("referralLinks", referralRepo.findAll());
        log.info("Dashboard carregado: {} contatos, {} vendas", contactRepo.count(), saleRepo.count());
        return "admin/dashboard";
    }

    // ---- CONTACTS ----

    // GET /admin/contatos — lista todos os contatos com breakdown por canal e referral
    @GetMapping("/contatos")
    public String contatos(Model model) {
        model.addAttribute("contacts", contactRepo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("sourceCounts", contactRepo.countBySource());
        model.addAttribute("referralCounts", contactRepo.countByReferralCode());
        return "admin/contatos";
    }

    // POST /admin/contatos/delete/{id} — exclui um contato e redireciona para a lista
    @PostMapping("/contatos/delete/{id}")
    public String deleteContato(@PathVariable Long id) {
        contactRepo.deleteById(id);
        return "redirect:/admin/contatos";
    }

    // ---- REFERRAL LINKS ----

    // POST /admin/referral/add — cria um novo código de indicação (exibido no dashboard)
    @PostMapping("/referral/add")
    public String addReferral(@RequestParam String code, @RequestParam String referrerName) {
        var ref = new ReferralLink();
        ref.setCode(code.toUpperCase()); // sempre maiúsculas para uniformidade na busca
        ref.setReferrerName(referrerName);
        referralRepo.save(ref);
        log.info("Referral adicionado: {}", code.toUpperCase());
        return "redirect:/admin";
    }

    // POST /admin/referral/delete/{id} — remove um código de indicação
    @PostMapping("/referral/delete/{id}")
    public String deleteReferral(@PathVariable Long id) {
        referralRepo.deleteById(id);
        return "redirect:/admin";
    }
}
