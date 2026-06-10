package com.confeitaria.controller;

import com.confeitaria.model.Sale;
import com.confeitaria.repository.RecipeRepository;
import com.confeitaria.repository.SaleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// Gerencia o registro e consulta de vendas no painel admin.
// Calcula os totais e prepara os dados dos três gráficos da página:
//   - Gráfico de linha: evolução diária (custo, receita, lucro) via dailySummaryByPeriod
//   - Gráfico de barras: por produto (receita/custo/lucro) via productSummaryByPeriod
//   - Gráfico de rosca: por grupo via groupSummaryByPeriod
// Por padrão exibe o mês atual; filtro de período via query params ?inicio=&fim=
// Depende de: SaleRepository (queries analíticas), RecipeRepository (dropdown para vincular venda)
@Slf4j
@Controller
@RequestMapping("/admin")
public class SaleController {

    private final SaleRepository saleRepo;
    private final RecipeRepository recipeRepo;

    public SaleController(SaleRepository saleRepo, RecipeRepository recipeRepo) {
        this.saleRepo = saleRepo;
        this.recipeRepo = recipeRepo;
    }

    // GET /admin/vendas?inicio=&fim= — exibe KPIs, gráficos e tabelas do período selecionado
    @GetMapping("/vendas")
    public String vendas(Model model,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        // Padrão: do primeiro ao último dia do mês atual
        if (inicio == null) inicio = LocalDate.now().withDayOfMonth(1);
        if (fim == null) fim = LocalDate.now();

        var sales = saleRepo.findBySaleDateBetweenOrderBySaleDateAsc(inicio, fim);
        var allSales = saleRepo.findAllByOrderBySaleDateDesc(); // tabela completa (sem filtro de período)

        // KPIs do período
        BigDecimal totalCost = sales.stream().map(Sale::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = sales.stream().map(Sale::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = sales.stream().map(Sale::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Dados do gráfico de linha diário: arrays paralelos de labels e valores
        var dailySummary = saleRepo.dailySummaryByPeriod(inicio, fim);
        var chartLabels = dailySummary.stream().map(r -> r[0].toString()).collect(Collectors.toList());
        var chartCosts = dailySummary.stream().map(r -> r[1]).collect(Collectors.toList());
        var chartRevenues = dailySummary.stream().map(r -> r[2]).collect(Collectors.toList());
        var chartProfits = dailySummary.stream().map(r -> r[3]).collect(Collectors.toList());

        // Dados das tabelas e gráficos de produto/grupo
        var productSummary = saleRepo.productSummaryByPeriod(inicio, fim);
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
        model.addAttribute("recipes", recipeRepo.findAllByOrderByNameAsc()); // dropdown de vínculo
        model.addAttribute("newSale", new Sale());

        // Lista de grupos únicos para o datalist de autocomplete no formulário
        var groups = allSales.stream().map(Sale::getProductGroup).filter(Objects::nonNull)
                .distinct().sorted().collect(Collectors.toList());
        model.addAttribute("productGroups", groups);

        return "admin/vendas";
    }

    // POST /admin/vendas/add — registra nova venda, opcionalmente vinculada a uma Recipe
    @PostMapping("/vendas/add")
    public String addSale(@ModelAttribute Sale sale,
                          @RequestParam(required = false) Long recipeId) {
        if (recipeId != null) {
            recipeRepo.findById(recipeId).ifPresent(sale::setRecipe);
        }
        if (sale.getSaleDate() == null) sale.setSaleDate(LocalDate.now());
        saleRepo.save(sale);
        log.info("Venda registrada: {}", sale.getProductName());
        return "redirect:/admin/vendas";
    }

    // POST /admin/vendas/delete/{id} — exclui a venda e recarrega a página
    @PostMapping("/vendas/delete/{id}")
    public String deleteSale(@PathVariable Long id) {
        saleRepo.deleteById(id);
        return "redirect:/admin/vendas";
    }
}
