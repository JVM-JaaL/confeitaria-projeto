package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

// Ingrediente cadastrado no sistema com preço por unidade (kg, L ou unidade).
// É a base do cálculo de custo das receitas: RecipeIngredient.getTotalCost() usa pricePerKg e unit.
// Gerenciado em: /admin/ingredientes (RecipeController)
// Usado por: RecipeIngredient (associação), receita-detalhe.html (listagem)
@Data
@Entity
@Table(name = "ingredients")
public class Ingredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    // Preço por kg (ou por litro, ou por unidade — depende do campo unit)
    private BigDecimal pricePerKg;
    // "kg" = gramas/1000 × pricePerKg; "L" = ml/1000 × pricePerKg; "un" = quantidade × pricePerKg
    private String unit = "kg";
}
