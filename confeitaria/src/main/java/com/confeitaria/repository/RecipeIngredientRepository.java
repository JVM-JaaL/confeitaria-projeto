package com.confeitaria.repository;

import com.confeitaria.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

// Acesso ao banco para RecipeIngredient (linhas da lista de ingredientes de uma receita).
// Usado por: RecipeController (adiciona/remove ingredientes de uma receita)
// Nota: a deleção em cascata da Recipe já remove os filhos automaticamente via CascadeType.ALL
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {}
