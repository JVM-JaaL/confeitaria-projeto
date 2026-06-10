package com.confeitaria.repository;

import com.confeitaria.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Acesso ao banco para Ingredient.
// Usado por: RecipeController (CRUD de ingredientes e montagem de receitas)
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // Lista todos os ingredientes em ordem alfabética — exibido nos dropdowns de receita
    List<Ingredient> findAllByOrderByNameAsc();
}
