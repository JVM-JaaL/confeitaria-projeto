package com.confeitaria.service;

import com.confeitaria.model.Sale;
import com.confeitaria.repository.SaleRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class FinanceAnalyticsService {

    private final SaleRepository saleRepo;

    public FinanceAnalyticsService(SaleRepository saleRepo) {
        this.saleRepo = saleRepo;
    }

    public FinanceReport buildReport(LocalDate inicio, LocalDate fim, String produtoFiltro, String grupoFiltro) {
        List<Sale> inRange = saleRepo.findBySaleDateBetweenOrderBySaleDateAsc(inicio, fim);
        String pNorm = produtoFiltro != null ? produtoFiltro.trim().toLowerCase() : "";
        String gNorm = grupoFiltro != null ? grupoFiltro.trim() : "";

        List<Sale> filtered = inRange.stream()
                .filter(s -> pNorm.isEmpty() || (s.getProductName() != null
                        && s.getProductName().toLowerCase().contains(pNorm)))
                .filter(s -> gNorm.isEmpty() || Objects.equals(s.getProductGroup(), gNorm))
                .toList();

        BigDecimal totalCost = sum(filtered, Sale::getCost);
        BigDecimal totalRevenue = sum(filtered, Sale::getRevenue);
        BigDecimal totalProfit = sum(filtered, Sale::getProfit);
        BigDecimal totalQty = filtered.stream()
                .map(s -> s.getQuantity() != null ? s.getQuantity() : BigDecimal.ONE)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal marginPct = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            marginPct = totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        Map<String, ProductAgg> byProduct = new LinkedHashMap<>();
        for (Sale s : filtered) {
            String name = s.getProductName() != null ? s.getProductName() : "(sem nome)";
            byProduct.computeIfAbsent(name, k -> new ProductAgg(name, s.getProductGroup()))
                    .add(s);
        }

        List<ProductMetric> productMetrics = byProduct.values().stream()
                .map(ProductAgg::toMetric)
                .sorted(Comparator.comparing(ProductMetric::getProfit).reversed())
                .toList();

        Map<LocalDate, DailyAgg> byDay = new TreeMap<>();
        for (Sale s : filtered) {
            LocalDate d = s.getSaleDate();
            if (d == null) continue;
            byDay.computeIfAbsent(d, k -> new DailyAgg()).add(s);
        }
        List<DailyPoint> dailyPoints = byDay.entrySet().stream()
                .map(e -> new DailyPoint(e.getKey(), e.getValue().revenue, e.getValue().cost, e.getValue().profit))
                .toList();

        Map<String, ProductAgg> byProductMonth = new LinkedHashMap<>();
        for (Sale s : filtered) {
            if (s.getSaleDate() == null) continue;
            String key = YearMonth.from(s.getSaleDate()) + " | " + (s.getProductName() != null ? s.getProductName() : "(sem nome)");
            byProductMonth.computeIfAbsent(key, k -> new ProductAgg(key, null)).add(s);
        }
        List<ProductMonthPoint> productMonthPoints = byProductMonth.values().stream()
                .map(a -> new ProductMonthPoint(a.name, a.revenue, a.cost, a.profit, a.quantity))
                .sorted(Comparator.comparing(ProductMonthPoint::getProfit).reversed())
                .toList();

        List<ProductMetric> topLucro = productMetrics.stream().limit(10).toList();
        List<ProductMetric> menosLucro = productMetrics.stream()
                .sorted(Comparator.comparing(ProductMetric::getProfit))
                .limit(10)
                .toList();
        List<ProductMetric> pioresMargem = productMetrics.stream()
                .filter(m -> m.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(ProductMetric::getMarginPercent))
                .limit(10)
                .toList();
        List<ProductMetric> melhoresMargem = productMetrics.stream()
                .filter(m -> m.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(ProductMetric::getMarginPercent).reversed())
                .limit(10)
                .toList();
        List<ProductMetric> maisPopulares = productMetrics.stream()
                .sorted(Comparator.comparing(ProductMetric::getQuantity).reversed())
                .limit(10)
                .toList();

        return new FinanceReport(inicio, fim, produtoFiltro, grupoFiltro, filtered.size(),
                totalCost, totalRevenue, totalProfit, totalQty, marginPct,
                productMetrics, dailyPoints, productMonthPoints,
                topLucro, menosLucro, pioresMargem, melhoresMargem, maisPopulares);
    }

    private static BigDecimal sum(List<Sale> sales, java.util.function.Function<Sale, BigDecimal> fn) {
        return sales.stream().map(fn).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static class ProductAgg {
        String name;
        String group;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;

        ProductAgg(String name, String group) {
            this.name = name;
            this.group = group;
        }

        void add(Sale s) {
            BigDecimal q = s.getQuantity() != null ? s.getQuantity() : BigDecimal.ONE;
            quantity = quantity.add(q);
            if (s.getRevenue() != null) revenue = revenue.add(s.getRevenue());
            if (s.getCost() != null) cost = cost.add(s.getCost());
            profit = profit.add(s.getProfit());
        }

        ProductMetric toMetric() {
            BigDecimal margin = BigDecimal.ZERO;
            if (revenue.compareTo(BigDecimal.ZERO) > 0) {
                margin = profit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            }
            return new ProductMetric(name, group, revenue, cost, profit, quantity, margin);
        }
    }

    private static class DailyAgg {
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;

        void add(Sale s) {
            if (s.getRevenue() != null) revenue = revenue.add(s.getRevenue());
            if (s.getCost() != null) cost = cost.add(s.getCost());
            profit = profit.add(s.getProfit());
        }
    }

    @Getter
    public static class FinanceReport {
        private final LocalDate inicio;
        private final LocalDate fim;
        private final String produtoFiltro;
        private final String grupoFiltro;
        private final int saleCount;
        private final BigDecimal totalCost;
        private final BigDecimal totalRevenue;
        private final BigDecimal totalProfit;
        private final BigDecimal totalQuantity;
        private final BigDecimal marginPercent;
        private final List<ProductMetric> productMetrics;
        private final List<DailyPoint> dailyPoints;
        private final List<ProductMonthPoint> productMonthPoints;
        private final List<ProductMetric> topByProfit;
        private final List<ProductMetric> leastByProfit;
        private final List<ProductMetric> worstMargin;
        private final List<ProductMetric> bestMargin;
        private final List<ProductMetric> mostPopular;

        public FinanceReport(LocalDate inicio, LocalDate fim, String produtoFiltro, String grupoFiltro, int saleCount,
                             BigDecimal totalCost, BigDecimal totalRevenue, BigDecimal totalProfit, BigDecimal totalQuantity,
                             BigDecimal marginPercent, List<ProductMetric> productMetrics, List<DailyPoint> dailyPoints,
                             List<ProductMonthPoint> productMonthPoints, List<ProductMetric> topByProfit,
                             List<ProductMetric> leastByProfit,
                             List<ProductMetric> worstMargin, List<ProductMetric> bestMargin, List<ProductMetric> mostPopular) {
            this.inicio = inicio;
            this.fim = fim;
            this.produtoFiltro = produtoFiltro;
            this.grupoFiltro = grupoFiltro;
            this.saleCount = saleCount;
            this.totalCost = totalCost;
            this.totalRevenue = totalRevenue;
            this.totalProfit = totalProfit;
            this.totalQuantity = totalQuantity;
            this.marginPercent = marginPercent;
            this.productMetrics = productMetrics;
            this.dailyPoints = dailyPoints;
            this.productMonthPoints = productMonthPoints;
            this.topByProfit = topByProfit;
            this.leastByProfit = leastByProfit;
            this.worstMargin = worstMargin;
            this.bestMargin = bestMargin;
            this.mostPopular = mostPopular;
        }
    }

    @Getter
    public static class ProductMetric {
        private final String productName;
        private final String productGroup;
        private final BigDecimal revenue;
        private final BigDecimal cost;
        private final BigDecimal profit;
        private final BigDecimal quantity;
        private final BigDecimal marginPercent;

        public ProductMetric(String productName, String productGroup, BigDecimal revenue, BigDecimal cost,
                             BigDecimal profit, BigDecimal quantity, BigDecimal marginPercent) {
            this.productName = productName;
            this.productGroup = productGroup;
            this.revenue = revenue;
            this.cost = cost;
            this.profit = profit;
            this.quantity = quantity;
            this.marginPercent = marginPercent;
        }
    }

    @Getter
    public static class DailyPoint {
        private final LocalDate date;
        private final BigDecimal revenue;
        private final BigDecimal cost;
        private final BigDecimal profit;

        public DailyPoint(LocalDate date, BigDecimal revenue, BigDecimal cost, BigDecimal profit) {
            this.date = date;
            this.revenue = revenue;
            this.cost = cost;
            this.profit = profit;
        }
    }

    @Getter
    public static class ProductMonthPoint {
        private final String label;
        private final BigDecimal revenue;
        private final BigDecimal cost;
        private final BigDecimal profit;
        private final BigDecimal quantity;

        public ProductMonthPoint(String label, BigDecimal revenue, BigDecimal cost, BigDecimal profit, BigDecimal quantity) {
            this.label = label;
            this.revenue = revenue;
            this.cost = cost;
            this.profit = profit;
            this.quantity = quantity;
        }
    }
}
