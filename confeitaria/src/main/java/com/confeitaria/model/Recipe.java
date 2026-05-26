package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private BigDecimal yieldGrams; // how many grams the recipe yields
    private String yieldDescription; // e.g. "1 bolo 20cm" or "30 brigadeiros"

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    /** Soma dos custos dos ingredientes (sem margem). Base para os demais cálculos. */
    public BigDecimal getIngredientCostRaw() {
        return ingredients.stream()
                .map(RecipeIngredient::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Custo marginal apenas sobre ingredientes (+5% perdas/desperdício). Gastos fixos entram na camada de serviço/UI. */
    public BigDecimal getMarginalCost() {
        return getIngredientCostRaw().multiply(new BigDecimal("1.05"));
    }

    /** Igual ao custo marginal de ingredientes — mantido para compatibilidade com telas que usam "custo total" só de ingredientes. */
    public BigDecimal getCostTotal() {
        return getMarginalCost();
    }

    /** Preço sugerido só com base em ingredientes × 3; na receita detalhada use o valor que inclui rateio mensal quando aplicável. */
    public BigDecimal getRecommendedPrice() {
        return getMarginalCost().multiply(new BigDecimal("3.0"));
    }

    public BigDecimal getCostPerGram() {
        if (yieldGrams == null || yieldGrams.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getMarginalCost().divide(yieldGrams, 4, java.math.RoundingMode.HALF_UP);
    }
}
