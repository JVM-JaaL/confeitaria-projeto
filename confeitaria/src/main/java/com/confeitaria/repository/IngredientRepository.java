package com.confeitaria.repository;
import com.confeitaria.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findAllByOrderByNameAsc();
}
