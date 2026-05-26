package com.confeitaria.controller;

import com.confeitaria.model.CostSettings;
import com.confeitaria.model.MonthlyExpense;
import com.confeitaria.repository.CostSettingsRepository;
import com.confeitaria.repository.MonthlyExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

@Controller
@RequestMapping("/admin/gastos-mensais")
public class MonthlyExpenseController {

    private final MonthlyExpenseRepository monthlyExpenseRepo;
    private final CostSettingsRepository costSettingsRepo;

    public MonthlyExpenseController(MonthlyExpenseRepository monthlyExpenseRepo,
                                    CostSettingsRepository costSettingsRepo) {
        this.monthlyExpenseRepo = monthlyExpenseRepo;
        this.costSettingsRepo = costSettingsRepo;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String mes, Model model) {
        YearMonth ym = parseMonthOrNow(mes);
        String ymStr = ym.toString();

        CostSettings settings = costSettingsRepo.findById(CostSettings.SINGLETON_ID).orElseGet(this::defaultCostSettings);

        BigDecimal monthTotal = monthlyExpenseRepo.sumAmountByYearMonth(ymStr);
        BigDecimal units = settings.getEstimatedMonthlyProductionUnits();
        BigDecimal fixedPerUnit = BigDecimal.ZERO;
        if (units != null && units.compareTo(BigDecimal.ZERO) > 0) {
            fixedPerUnit = monthTotal.divide(units, 4, RoundingMode.HALF_UP);
        }

        model.addAttribute("currentPage", "gastos-mensais");
        model.addAttribute("pageTitle", "Gastos mensais");
        model.addAttribute("yearMonth", ymStr);
        model.addAttribute("prevMonth", ym.minusMonths(1).toString());
        model.addAttribute("nextMonth", ym.plusMonths(1).toString());
        model.addAttribute("expenses", monthlyExpenseRepo.findByYearMonthOrderByIdDesc(ymStr));
        model.addAttribute("monthTotal", monthTotal);
        model.addAttribute("fixedPerProductionUnit", fixedPerUnit);
        model.addAttribute("costSettings", settings);
        model.addAttribute("newExpense", blankExpense(ymStr));
        return "admin/gastos-mensais";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute MonthlyExpense expense,
                      @RequestParam(required = false) String mes) {
        if (expense.getYearMonth() == null || expense.getYearMonth().isBlank()) {
            expense.setYearMonth(parseMonthOrNow(mes).toString());
        }
        monthlyExpenseRepo.save(expense);
        return "redirect:/admin/gastos-mensais?mes=" + expense.getYearMonth();
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @RequestParam(required = false) String mes) {
        monthlyExpenseRepo.deleteById(id);
        String redirectMonth = mes != null ? mes : YearMonth.now().toString();
        return "redirect:/admin/gastos-mensais?mes=" + redirectMonth;
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute CostSettings form,
                               @RequestParam(required = false) String mes) {
        CostSettings existing = costSettingsRepo.findById(CostSettings.SINGLETON_ID).orElseGet(this::defaultCostSettings);
        if (form.getEstimatedMonthlyProductionUnits() != null
                && form.getEstimatedMonthlyProductionUnits().compareTo(BigDecimal.ZERO) > 0) {
            existing.setEstimatedMonthlyProductionUnits(form.getEstimatedMonthlyProductionUnits());
        }
        costSettingsRepo.save(existing);
        String m = mes != null ? mes : YearMonth.now().toString();
        return "redirect:/admin/gastos-mensais?mes=" + m;
    }

    private YearMonth parseMonthOrNow(String mes) {
        if (mes == null || mes.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(mes);
        } catch (Exception e) {
            return YearMonth.now();
        }
    }

    private MonthlyExpense blankExpense(String ymStr) {
        var e = new MonthlyExpense();
        e.setYearMonth(ymStr);
        e.setAmount(BigDecimal.ZERO);
        return e;
    }

    private CostSettings defaultCostSettings() {
        var s = new CostSettings();
        s.setId(CostSettings.SINGLETON_ID);
        return s;
    }
}
