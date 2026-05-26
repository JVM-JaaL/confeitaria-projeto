package com.confeitaria.controller;

import com.confeitaria.model.*;
import com.confeitaria.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class RecipeController {

    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeIngredientRepository recipeIngredientRepo;
    private final MonthlyExpenseRepository monthlyExpenseRepo;
    private final CostSettingsRepository costSettingsRepo;

    public RecipeController(RecipeRepository recipeRepo, IngredientRepository ingredientRepo,
                            RecipeIngredientRepository recipeIngredientRepo,
                            MonthlyExpenseRepository monthlyExpenseRepo,
                            CostSettingsRepository costSettingsRepo) {
        this.recipeRepo = recipeRepo;
        this.ingredientRepo = ingredientRepo;
        this.recipeIngredientRepo = recipeIngredientRepo;
        this.monthlyExpenseRepo = monthlyExpenseRepo;
        this.costSettingsRepo = costSettingsRepo;
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
        BigDecimal fixedPerUnit = computeFixedAllocationPerUnit(ym.toString());
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
        YearMonth ym = parseYearMonth(mes);
        String ymStr = ym.toString();
        var recipe = recipeRepo.findById(id).orElseThrow();

        BigDecimal monthlyFixed = monthlyExpenseRepo.sumAmountByYearMonth(ymStr);
        CostSettings settings = costSettingsRepo.findById(CostSettings.SINGLETON_ID).orElse(null);
        BigDecimal units = settings != null ? settings.getEstimatedMonthlyProductionUnits() : BigDecimal.ZERO;
        BigDecimal fixedPerUnit = BigDecimal.ZERO;
        if (units != null && units.compareTo(BigDecimal.ZERO) > 0) {
            fixedPerUnit = monthlyFixed.divide(units, 4, RoundingMode.HALF_UP);
        }

        BigDecimal marginal = recipe.getMarginalCost();
        BigDecimal fullProductionCost = marginal.add(fixedPerUnit);
        BigDecimal recommendedSalePrice = fullProductionCost.multiply(new BigDecimal("3.0"));

        BigDecimal costPerGramFull = BigDecimal.ZERO;
        if (recipe.getYieldGrams() != null && recipe.getYieldGrams().compareTo(BigDecimal.ZERO) > 0) {
            costPerGramFull = fullProductionCost.divide(recipe.getYieldGrams(), 4, RoundingMode.HALF_UP);
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

    private BigDecimal computeFixedAllocationPerUnit(String yearMonth) {
        BigDecimal monthlyFixed = monthlyExpenseRepo.sumAmountByYearMonth(yearMonth);
        CostSettings settings = costSettingsRepo.findById(CostSettings.SINGLETON_ID).orElse(null);
        if (settings == null || settings.getEstimatedMonthlyProductionUnits() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal units = settings.getEstimatedMonthlyProductionUnits();
        if (units.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return monthlyFixed.divide(units, 4, RoundingMode.HALF_UP);
    }

    private static YearMonth parseYearMonth(String mes) {
        if (mes == null || mes.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(mes);
        } catch (Exception e) {
            return YearMonth.now();
        }
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
