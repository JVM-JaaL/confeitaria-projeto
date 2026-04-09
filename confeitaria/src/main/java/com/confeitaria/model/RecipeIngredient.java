package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private BigDecimal quantityGrams; // amount used in grams

    public BigDecimal getTotalCost() {
        if (ingredient == null || ingredient.getPricePerKg() == null || quantityGrams == null) return BigDecimal.ZERO;
        // convert grams to kg then multiply by price
        BigDecimal kg = quantityGrams.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
        return kg.multiply(ingredient.getPricePerKg()).setScale(4, RoundingMode.HALF_UP);
    }
}
