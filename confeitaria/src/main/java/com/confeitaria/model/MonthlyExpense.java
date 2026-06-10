package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

// Representa um gasto em um determinado mês (formato yyyy-MM).
// FIXO: recorrente — entra no rateio de custo fixo por unidade de produção (RecipeService).
// EVENTUAL: pontual — registrado mas não divide o custo por receita.
// Gerenciado em: /admin/gastos-mensais (MonthlyExpenseController)
// Lido por: RecipeService.computeFixedAllocationPerUnit(), MonthlyExpenseController
@Data
@Entity
@Table(name = "monthly_expenses")
public class MonthlyExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato yyyy-MM (ex.: 2026-05) — indexa os gastos por mês */
    @Column(nullable = false, length = 7)
    private String yearMonth;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal amount;

    /** FIXO = recorrente (aluguel, luz…); EVENTUAL = pontual (perda, roubo…) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'FIXO'")
    private ExpenseType type = ExpenseType.FIXO;
}
