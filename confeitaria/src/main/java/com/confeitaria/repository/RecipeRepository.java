package com.confeitaria.repository;

import com.confeitaria.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Acesso ao banco para Recipe.
// Usado por: RecipeController (CRUD), SaleController (dropdown para vincular venda), AdminController (contagem no dashboard)
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // Lista todas as receitas em ordem alfabética
    List<Recipe> findAllByOrderByNameAsc();

    // Lista receitas filtradas por categoria (ex: "bolo") — disponível para uso futuro em filtros
    List<Recipe> findByCategoryOrderByNameAsc(String category);
}
