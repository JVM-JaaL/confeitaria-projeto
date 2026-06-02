package com.confeitaria.repository;
import com.confeitaria.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findAllByOrderByNameAsc();
    List<Recipe> findByCategoryOrderByNameAsc(String category);
}
