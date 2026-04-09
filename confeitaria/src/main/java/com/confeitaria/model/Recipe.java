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

    // Calculated: sum of all ingredient costs * 1.05
    public BigDecimal getCostTotal() {
        BigDecimal total = ingredients.stream()
            .map(RecipeIngredient::getTotalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.multiply(new BigDecimal("1.05")); // +5% marginal cost
    }

    // Recommended price: cost * 3 (typical confeitaria markup)
    public BigDecimal getRecommendedPrice() {
        return getCostTotal().multiply(new BigDecimal("3.0"));
    }

    public BigDecimal getCostPerGram() {
        if (yieldGrams == null || yieldGrams.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getCostTotal().divide(yieldGrams, 4, java.math.RoundingMode.HALF_UP);
    }
}
