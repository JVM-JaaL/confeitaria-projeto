package com.confeitaria.controller;

import com.confeitaria.model.Sale;
import com.confeitaria.repository.RecipeRepository;
import com.confeitaria.repository.SaleRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class SaleController {

    private final SaleRepository saleRepo;
    private final RecipeRepository recipeRepo;

    public SaleController(SaleRepository saleRepo, RecipeRepository recipeRepo) {
        this.saleRepo = saleRepo;
        this.recipeRepo = recipeRepo;
    }

    @GetMapping("/vendas")
    public String vendas(Model model,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        if (inicio == null) inicio = LocalDate.now().withDayOfMonth(1);
        if (fim == null) fim = LocalDate.now();

        var sales = saleRepo.findBySaleDateBetweenOrderBySaleDateAsc(inicio, fim);
        var allSales = saleRepo.findAllByOrderBySaleDateDesc();

        // Totals
        BigDecimal totalCost = sales.stream().map(Sale::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = sales.stream().map(Sale::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = sales.stream().map(Sale::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Daily chart data
        var dailySummary = saleRepo.dailySummaryByPeriod(inicio, fim);
        var chartLabels = dailySummary.stream().map(r -> r[0].toString()).collect(Collectors.toList());
        var chartCosts = dailySummary.stream().map(r -> r[1]).collect(Collectors.toList());
        var chartRevenues = dailySummary.stream().map(r -> r[2]).collect(Collectors.toList());
        var chartProfits = dailySummary.stream().map(r -> r[3]).collect(Collectors.toList());

        // Product summary
        var productSummary = saleRepo.productSummaryByPeriod(inicio, fim);
        // Group summary
        var groupSummary = saleRepo.groupSummaryByPeriod(inicio, fim);

        model.addAttribute("sales", allSales);
        model.addAttribute("filteredSales", sales);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalProfit", totalProfit);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartCosts", chartCosts);
        model.addAttribute("chartRevenues", chartRevenues);
        model.addAttribute("chartProfits", chartProfits);
        model.addAttribute("productSummary", productSummary);
        model.addAttribute("groupSummary", groupSummary);
        model.addAttribute("recipes", recipeRepo.findAllByOrderByNameAsc());
        model.addAttribute("newSale", new Sale());

        // Collect all unique product groups for filter
        var groups = allSales.stream().map(Sale::getProductGroup).filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
        model.addAttribute("productGroups", groups);

        return "admin/vendas";
    }

    @PostMapping("/vendas/add")
    public String addSale(@ModelAttribute Sale sale,
                          @RequestParam(required = false) Long recipeId) {
        if (recipeId != null) {
            recipeRepo.findById(recipeId).ifPresent(sale::setRecipe);
        }
        if (sale.getSaleDate() == null) sale.setSaleDate(LocalDate.now());
        saleRepo.save(sale);
        return "redirect:/admin/vendas";
    }

    @PostMapping("/vendas/delete/{id}")
    public String deleteSale(@PathVariable Long id) {
        saleRepo.deleteById(id);
        return "redirect:/admin/vendas";
    }
}
