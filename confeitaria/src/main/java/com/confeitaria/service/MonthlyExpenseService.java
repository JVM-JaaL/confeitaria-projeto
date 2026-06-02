package com.confeitaria.service;

import com.confeitaria.model.CostSettings;
import com.confeitaria.model.ExpenseType;
import com.confeitaria.model.MonthlyExpense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
public class MonthlyExpenseService {

    private final RecipeService recipeService;

    public MonthlyExpenseService(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    public YearMonth parseMonthOrNow(String mes) {
        return recipeService.parseYearMonth(mes);
    }

    public MonthlyExpense blankExpense(String ymStr) {
        var e = new MonthlyExpense();
        e.setYearMonth(ymStr);
        e.setAmount(BigDecimal.ZERO);
        e.setType(ExpenseType.FIXO);
        return e;
    }

    public CostSettings defaultCostSettings() {
        var s = new CostSettings();
        s.setId(CostSettings.SINGLETON_ID);
        return s;
    }
}
