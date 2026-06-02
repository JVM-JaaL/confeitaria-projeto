package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "sales")
public class Sale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private String productGroup; // category/group for grouping in charts

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe; // optional link to recipe

    private BigDecimal cost;    // custo total
    private BigDecimal revenue; // receita (selling price)
    private BigDecimal quantity = BigDecimal.ONE;

    private LocalDate saleDate;
    private String notes;

    public BigDecimal getProfit() {
        if (revenue == null || cost == null) return BigDecimal.ZERO;
        return revenue.subtract(cost);
    }

    public BigDecimal getMargin() {
        if (revenue == null || revenue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getProfit().divide(revenue, 4, java.math.RoundingMode.HALF_UP)
                         .multiply(new BigDecimal("100"));
    }
}
