package com.confeitaria.controller;

import com.confeitaria.model.*;
import com.confeitaria.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class RecipeController {

    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeIngredientRepository recipeIngredientRepo;

    public RecipeController(RecipeRepository recipeRepo, IngredientRepository ingredientRepo,
                            RecipeIngredientRepository recipeIngredientRepo) {
        this.recipeRepo = recipeRepo;
        this.ingredientRepo = ingredientRepo;
        this.recipeIngredientRepo = recipeIngredientRepo;
    }

    // ---- INGREDIENTS ----
    @GetMapping("/ingredientes")
    public String ingredientes(Model model) {
        model.addAttribute("ingredients", ingredientRepo.findAllByOrderByNameAsc());
        model.addAttribute("newIng", new Ingredient());
        return "admin/ingredientes";
    }

    @PostMapping("/ingredientes/add")
    public String addIngredient(@ModelAttribute Ingredient ing) {
        ingredientRepo.save(ing);
        return "redirect:/admin/ingredientes";
    }

    @PostMapping("/ingredientes/delete/{id}")
    public String deleteIngredient(@PathVariable Long id) {
        ingredientRepo.deleteById(id);
        return "redirect:/admin/ingredientes";
    }

    @PostMapping("/ingredientes/edit/{id}")
    public String editIngredient(@PathVariable Long id, @RequestParam BigDecimal pricePerKg) {
        ingredientRepo.findById(id).ifPresent(ing -> {
            ing.setPricePerKg(pricePerKg);
            ingredientRepo.save(ing);
        });
        return "redirect:/admin/ingredientes";
    }

    // ---- RECIPES ----
    @GetMapping("/receitas")
    public String receitas(Model model) {
        model.addAttribute("recipes", recipeRepo.findAllByOrderByNameAsc());
        model.addAttribute("ingredients", ingredientRepo.findAllByOrderByNameAsc());
        model.addAttribute("newRecipe", new Recipe());
        return "admin/receitas";
    }

    @GetMapping("/receitas/{id}")
    public String recipeDetail(@PathVariable Long id, Model model) {
        var recipe = recipeRepo.findById(id).orElseThrow();
        model.addAttribute("recipe", recipe);
        model.addAttribute("allIngredients", ingredientRepo.findAllByOrderByNameAsc());
        return "admin/receita-detalhe";
    }

    @PostMapping("/receitas/add")
    public String addRecipe(@ModelAttribute Recipe recipe) {
        recipe.setIngredients(new ArrayList<>());
        recipeRepo.save(recipe);
        return "redirect:/admin/receitas/" + recipe.getId();
    }

    @PostMapping("/receitas/{id}/addIngredient")
    public String addRecipeIngredient(@PathVariable Long id,
                                      @RequestParam Long ingredientId,
                                      @RequestParam BigDecimal quantityGrams) {
        var recipe = recipeRepo.findById(id).orElseThrow();
        var ingredient = ingredientRepo.findById(ingredientId).orElseThrow();
        var ri = new RecipeIngredient();
        ri.setRecipe(recipe);
        ri.setIngredient(ingredient);
        ri.setQuantityGrams(quantityGrams);
        recipeIngredientRepo.save(ri);
        return "redirect:/admin/receitas/" + id;
    }

    @PostMapping("/receitas/{id}/removeIngredient/{riId}")
    public String removeRecipeIngredient(@PathVariable Long id, @PathVariable Long riId) {
        recipeIngredientRepo.deleteById(riId);
        return "redirect:/admin/receitas/" + id;
    }

    @PostMapping("/receitas/delete/{id}")
    public String deleteRecipe(@PathVariable Long id) {
        recipeRepo.deleteById(id);
        return "redirect:/admin/receitas";
    }
}
