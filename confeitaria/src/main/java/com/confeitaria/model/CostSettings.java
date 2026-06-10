package com.confeitaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

// Configuração global de produção — existe sempre uma única linha (singleton, id=1).
// O campo estimatedMonthlyProductionUnits é usado por RecipeService para dividir o custo
// fixo mensal por unidade: fixedPerUnit = totalFixo / estimatedMonthlyProductionUnits
// Gerenciado em: /admin/gastos-mensais (formulário de configuração)
@Data
@Entity
@Table(name = "cost_settings")
public class CostSettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    /**
     * Quantidade média de unidades de produção (bateladas/receitas completas) vendidas por mês,
     * usada para ratear gastos fixos mensais sobre cada receita.
     */
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal estimatedMonthlyProductionUnits = new BigDecimal("100");
}
