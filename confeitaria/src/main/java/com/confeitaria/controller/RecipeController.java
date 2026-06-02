package com.confeitaria.controller;

import com.confeitaria.model.*;
import com.confeitaria.repository.*;
import com.confeitaria.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;

@Slf4j
@Controller
@RequestMapping("/admin")
public class RecipeController {

    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeIngredientRepository recipeIngredientRepo;
    private final RecipeService recipeService;

    public RecipeController(RecipeRepository recipeRepo, IngredientRepository ingredientRepo,
                            RecipeIngredientRepository recipeIngredientRepo,
                            RecipeService recipeService) {
        this.recipeRepo = recipeRepo;
        this.ingredientRepo = ingredientRepo;
        this.recipeIngredientRepo = recipeIngredientRepo;
        this.recipeService = recipeService;
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
        YearMonth ym = YearMonth.now();
        BigDecimal fixedPerUnit = recipeService.computeFixedAllocationPerUnit(ym.toString());
        model.addAttribute("recipes", recipeRepo.findAllByOrderByNameAsc());
        model.addAttribute("ingredients", ingredientRepo.findAllByOrderByNameAsc());
        model.addAttribute("newRecipe", new Recipe());
        model.addAttribute("fixedAllocationPerUnit", fixedPerUnit);
        model.addAttribute("costReferenceMonth", ym.toString());
        return "admin/receitas";
    }

    @GetMapping("/receitas/{id}")
    public String recipeDetail(@PathVariable Long id,
                               @RequestParam(required = false) String mes,
                               Model model) {
        YearMonth ym = recipeService.parseYearMonth(mes);
        String ymStr = ym.toString();
        var recipe = recipeRepo.findById(id).orElseThrow();

        BigDecimal monthlyFixed = recipeService.getMonthlyFixed(ymStr);
        BigDecimal units = recipeService.getEstimatedUnits();
        BigDecimal fixedPerUnit = recipeService.computeFixedAllocationPerUnit(ymStr);

        log.debug("Calculando custo para receita {}, mês {}", id, ymStr);

        BigDecimal marginal = recipe.getMarginalCost();
        BigDecimal fullProductionCost = marginal.add(fixedPerUnit);
        BigDecimal recommendedSalePrice = fullProductionCost.multiply(new BigDecimal("3.0"));

        BigDecimal costPerGramFull = BigDecimal.ZERO;
        if (recipe.getYieldGrams() != null && recipe.getYieldGrams().compareTo(BigDecimal.ZERO) > 0) {
            costPerGramFull = fullProductionCost.divide(recipe.getYieldGrams(), 4, java.math.RoundingMode.HALF_UP);
        }

        model.addAttribute("recipe", recipe);
        model.addAttribute("allIngredients", ingredientRepo.findAllByOrderByNameAsc());
        model.addAttribute("referenceMonth", ymStr);
        model.addAttribute("prevMonth", ym.minusMonths(1).toString());
        model.addAttribute("nextMonth", ym.plusMonths(1).toString());
        model.addAttribute("monthlyFixedTotal", monthlyFixed);
        model.addAttribute("estimatedMonthlyUnits", units);
        model.addAttribute("fixedAllocationPerUnit", fixedPerUnit);
        model.addAttribute("fullProductionCost", fullProductionCost);
        model.addAttribute("recommendedSalePrice", recommendedSalePrice);
        model.addAttribute("costPerGramProduction", costPerGramFull);
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
