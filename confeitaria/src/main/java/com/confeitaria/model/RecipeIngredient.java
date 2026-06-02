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
        String unit = ingredient.getUnit() != null ? ingredient.getUnit() : "kg";
        if ("un".equalsIgnoreCase(unit)) {
            // quantityGrams stores number of units; price is per unit
            return quantityGrams.multiply(ingredient.getPricePerKg()).setScale(4, RoundingMode.HALF_UP);
        }
        // kg and L: quantity in g/ml, price per kg/L — same 1000 divisor
        BigDecimal base = quantityGrams.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
        return base.multiply(ingredient.getPricePerKg()).setScale(4, RoundingMode.HALF_UP);
    }
}
