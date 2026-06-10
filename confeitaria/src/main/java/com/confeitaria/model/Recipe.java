package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Receita culinária com lista de ingredientes e métodos de cálculo de custo.
// Os cálculos encadeiam: getIngredientCostRaw() → getMarginalCost() → getRecommendedPrice()
// O custo completo de produção (incluindo gastos fixos) é calculado fora, em RecipeController e RecipeService.
// Gerenciado em: /admin/receitas (RecipeController)
// Usado por: RecipeController, FinanceAnalyticsService, Sale (vínculo opcional)
@Data
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category; // bolo, torta, doce, etc
    @Column(length = 2000)
    private String description;
    private BigDecimal yieldGrams; // quantos gramas a receita rende
    private String yieldDescription; // ex: "1 bolo 20cm" ou "30 brigadeiros"

    // Lista de ingredientes com quantidade — cascade ALL: ao deletar a receita, os itens são apagados
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    // Soma bruta do custo de todos os ingredientes (sem margem)
    public BigDecimal getIngredientCostRaw() {
        return ingredients.stream()
                .map(RecipeIngredient::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Custo com +5% de margem para perdas e desperdício
    public BigDecimal getMarginalCost() {
        return getIngredientCostRaw().multiply(new BigDecimal("1.05"));
    }

    // Alias de getMarginalCost() — mantido para compatibilidade com templates que mostram custo total
    public BigDecimal getCostTotal() {
        return getMarginalCost();
    }

    // Preço sugerido só com ingredientes × 3 (markup 3×). Na tela de detalhes inclui o rateio mensal.
    public BigDecimal getRecommendedPrice() {
        return getMarginalCost().multiply(new BigDecimal("3.0"));
    }

    // Custo por grama — útil para calcular custo de porções menores
    public BigDecimal getCostPerGram() {
        if (yieldGrams == null || yieldGrams.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getMarginalCost().divide(yieldGrams, 4, java.math.RoundingMode.HALF_UP);
    }
}
