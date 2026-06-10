package com.confeitaria.service;

import com.confeitaria.model.CostSettings;
import com.confeitaria.model.ExpenseType;
import com.confeitaria.model.MonthlyExpense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;

// Helpers para o MonthlyExpenseController: parsing de mês, objetos em branco para formulários.
// Delega para RecipeService o parse de mês para evitar duplicação de lógica.
// Usado exclusivamente por: MonthlyExpenseController
@Service
public class MonthlyExpenseService {

    private final RecipeService recipeService;

    public MonthlyExpenseService(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // Converte "yyyy-MM" em YearMonth usando a lógica de RecipeService (retorna mês atual se inválido)
    public YearMonth parseMonthOrNow(String mes) {
        return recipeService.parseYearMonth(mes);
    }

    // Cria um MonthlyExpense em branco já com o mês e tipo padrão preenchidos — usado no formulário de adição
    public MonthlyExpense blankExpense(String ymStr) {
        var e = new MonthlyExpense();
        e.setYearMonth(ymStr);
        e.setAmount(BigDecimal.ZERO);
        e.setType(ExpenseType.FIXO);
        return e;
    }

    // Cria um CostSettings padrão (sem dados do banco) — usado como fallback quando o registro ainda não existe
    public CostSettings defaultCostSettings() {
        var s = new CostSettings();
        s.setId(CostSettings.SINGLETON_ID);
        return s;
    }
}
