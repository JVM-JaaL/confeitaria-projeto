package com.confeitaria.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.confeitaria.repository.SaleRepository;
import com.confeitaria.service.FinanceAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import com.confeitaria.service.FinanceAnalyticsService.DailyPoint;
import com.confeitaria.service.FinanceAnalyticsService.ProductMetric;
import com.confeitaria.service.FinanceAnalyticsService.ProductMonthPoint;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// Controller da página de análise financeira avançada (/admin/financeiro).
// Delega toda a lógica de agregação ao FinanceAnalyticsService e converte os resultados
// em JSON (via Jackson ObjectMapper) para os gráficos Chart.js no template financeiro.html.
// Os três blocos JSON são injetados via th:utext em <script type="application/json"> —
// o JavaScript lê com JSON.parse(element.textContent) para evitar o escape do th:text.
// Depende de: FinanceAnalyticsService (relatório), SaleRepository (lista de grupos para filtro)
@Slf4j
@Controller
@RequestMapping("/admin")
public class FinancialDashboardController {

    private final FinanceAnalyticsService financeAnalyticsService;
    private final SaleRepository saleRepo;
    private final ObjectMapper objectMapper; // injetado automaticamente pelo Spring Boot (Jackson)

    public FinancialDashboardController(FinanceAnalyticsService financeAnalyticsService,
                                        SaleRepository saleRepo,
                                        ObjectMapper objectMapper) {
        this.financeAnalyticsService = financeAnalyticsService;
        this.saleRepo = saleRepo;
        this.objectMapper = objectMapper;
    }

    // GET /admin/financeiro?inicio=&fim=&produto=&grupo=
    // Constrói o relatório, serializa os dados dos gráficos em JSON e popula o model
    @GetMapping("/financeiro")
    public String financeiro(Model model,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                             @RequestParam(required = false) String produto,
                             @RequestParam(required = false) String grupo) throws JsonProcessingException {
        if (inicio == null) {
            inicio = LocalDate.now().withDayOfMonth(1);
        }
        if (fim == null) {
            fim = LocalDate.now();
        }

        // Gera o relatório completo com todos os agrupamentos e rankings
        var report = financeAnalyticsService.buildReport(inicio, fim, produto, grupo);
        log.debug("Relatório financeiro gerado: {} vendas de {} a {}", report.getSaleCount(), inicio, fim);

        // Lista de grupos distintos para o dropdown de filtro
        Set<String> grupos = saleRepo.findAllByOrderBySaleDateDesc().stream()
                .map(s -> s.getProductGroup())
                .filter(Objects::nonNull)
                .filter(g -> !g.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Converte os dados dos gráficos para JSON — lidos pelo JavaScript em financeiro.html
        List<Map<String, Object>> dailyRows = report.getDailyPoints().stream().map(this::dailyRow).toList();
        List<Map<String, Object>> productRows = report.getProductMetrics().stream().map(this::productRow).toList();
        List<Map<String, Object>> pmRows = report.getProductMonthPoints().stream().map(this::productMonthRow).toList();

        model.addAttribute("dailyJson", objectMapper.writeValueAsString(dailyRows));
        model.addAttribute("productJson", objectMapper.writeValueAsString(productRows));
        model.addAttribute("productMonthJson", objectMapper.writeValueAsString(pmRows));

        model.addAttribute("currentPage", "financeiro");
        model.addAttribute("pageTitle", "Análise financeira");
        model.addAttribute("report", report);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("produto", produto != null ? produto : "");
        model.addAttribute("grupo", grupo != null ? grupo : "");
        model.addAttribute("productGroups", grupos);
        return "admin/financeiro";
    }

    // Converte DailyPoint em Map para serialização JSON — chaves usadas pelo Chart.js em financeiro.html
    private Map<String, Object> dailyRow(DailyPoint d) {
        Map<String, Object> m = new HashMap<>();
        m.put("date", d.getDate().toString());
        m.put("revenue", d.getRevenue());
        m.put("cost", d.getCost());
        m.put("profit", d.getProfit());
        return m;
    }

    // Converte ProductMetric em Map para serialização JSON
    private Map<String, Object> productRow(ProductMetric p) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", p.getProductName());
        m.put("group", p.getProductGroup());
        m.put("revenue", p.getRevenue());
        m.put("cost", p.getCost());
        m.put("profit", p.getProfit());
        m.put("quantity", p.getQuantity());
        m.put("marginPercent", p.getMarginPercent());
        return m;
    }

    // Converte ProductMonthPoint em Map para serialização JSON
    private Map<String, Object> productMonthRow(ProductMonthPoint p) {
        Map<String, Object> m = new HashMap<>();
        m.put("label", p.getLabel());
        m.put("revenue", p.getRevenue());
        m.put("cost", p.getCost());
        m.put("profit", p.getProfit());
        m.put("quantity", p.getQuantity());
        return m;
    }
}
