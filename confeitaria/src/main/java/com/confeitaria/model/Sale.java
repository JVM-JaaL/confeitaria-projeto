package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// Registro de uma venda realizada. Pode ser vinculada a uma Recipe (opcional).
// getProfit() e getMargin() são calculados em tempo real a partir de cost e revenue.
// Usado por: SaleController (CRUD), FinanceAnalyticsService (relatórios e gráficos)
// Gerenciado em: /admin/vendas
@Data
@Entity
@Table(name = "sales")
public class Sale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    // Grupo/categoria usado para agrupar nos gráficos (ex: "Bolo", "Doce Fino")
    private String productGroup;

    // Vínculo opcional com receita para rastreabilidade
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    private BigDecimal cost;    // custo total da venda
    private BigDecimal revenue; // valor recebido (preço de venda)
    private BigDecimal quantity = BigDecimal.ONE;

    private LocalDate saleDate;
    private String notes;

    // Lucro = receita − custo
    public BigDecimal getProfit() {
        if (revenue == null || cost == null) return BigDecimal.ZERO;
        return revenue.subtract(cost);
    }

    // Margem percentual = (lucro / receita) × 100
    public BigDecimal getMargin() {
        if (revenue == null || revenue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getProfit().divide(revenue, 4, java.math.RoundingMode.HALF_UP)
                         .multiply(new BigDecimal("100"));
    }
}
