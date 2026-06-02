package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "monthly_expenses")
public class MonthlyExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato yyyy-MM (ex.: 2026-05) */
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
