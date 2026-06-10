package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;

// Linha da "lista de ingredientes" de uma receita: liga Recipe ↔ Ingredient com uma quantidade.
// getTotalCost() converte a quantidade para a unidade correta do ingrediente e calcula o custo.
// Gerenciado em: /admin/receitas/{id} (RecipeController.addRecipeIngredient / removeRecipeIngredient)
@Data
@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Receita pai — ao deletar a Recipe, este registro é removido em cascata
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    // Ingrediente referenciado — contém preço e unidade
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    // Quantidade usada. Para kg/L = gramas ou ml; para "un" = número de unidades inteiras
    private BigDecimal quantityGrams;

    // Calcula o custo desta linha respeitando a unidade do ingrediente
    public BigDecimal getTotalCost() {
        if (ingredient == null || ingredient.getPricePerKg() == null || quantityGrams == null) return BigDecimal.ZERO;
        String unit = ingredient.getUnit() != null ? ingredient.getUnit() : "kg";
        if ("un".equalsIgnoreCase(unit)) {
            // Para unidades: quantidade × preço por unidade
            return quantityGrams.multiply(ingredient.getPricePerKg()).setScale(4, RoundingMode.HALF_UP);
        }
        // Para kg e L: divide por 1000 para converter g→kg ou ml→L
        BigDecimal base = quantityGrams.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
        return base.multiply(ingredient.getPricePerKg()).setScale(4, RoundingMode.HALF_UP);
    }
}
