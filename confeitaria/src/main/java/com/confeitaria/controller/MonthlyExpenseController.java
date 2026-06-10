package com.confeitaria.controller;

import com.confeitaria.model.CostSettings;
import com.confeitaria.model.ExpenseType;
import com.confeitaria.model.MonthlyExpense;
import com.confeitaria.repository.CostSettingsRepository;
import com.confeitaria.repository.MonthlyExpenseRepository;
import com.confeitaria.service.MonthlyExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

// Gerencia os gastos mensais (fixos e eventuais) e a configuração de unidades de produção.
// A página exibe totais separados por tipo e calcula o rateio fixo por unidade de produção:
//   fixedPerUnit = fixedTotal / settings.estimatedMonthlyProductionUnits
// Esse valor é exibido na tela e também é a base do cálculo em RecipeService.
// Navegação por mês via ?mes=yyyy-MM — setas de anterior/próximo geradas no template.
// Depende de: MonthlyExpenseService (utilitários de mês e objetos em branco), repositories
@Slf4j
@Controller
@RequestMapping("/admin/gastos-mensais")
public class MonthlyExpenseController {

    private final MonthlyExpenseRepository monthlyExpenseRepo;
    private final CostSettingsRepository costSettingsRepo;
    private final MonthlyExpenseService monthlyExpenseService;

    public MonthlyExpenseController(MonthlyExpenseRepository monthlyExpenseRepo,
                                    CostSettingsRepository costSettingsRepo,
                                    MonthlyExpenseService monthlyExpenseService) {
        this.monthlyExpenseRepo = monthlyExpenseRepo;
        this.costSettingsRepo = costSettingsRepo;
        this.monthlyExpenseService = monthlyExpenseService;
    }

    // GET /admin/gastos-mensais?mes=yyyy-MM — lista os gastos do mês com totais e rateio
    @GetMapping
    public String list(@RequestParam(required = false) String mes, Model model) {
        YearMonth ym = monthlyExpenseService.parseMonthOrNow(mes);
        String ymStr = ym.toString();

        // Carrega configuração de produção; se não existir, usa o default (100 unidades)
        CostSettings settings = costSettingsRepo.findById(CostSettings.SINGLETON_ID)
                .orElseGet(monthlyExpenseService::defaultCostSettings);

        List<MonthlyExpense> expenses = monthlyExpenseRepo.findByYearMonthOrderByIdDesc(ymStr);

        // Separa e soma gastos por tipo
        BigDecimal fixedTotal = expenses.stream()
                .filter(e -> e.getType() == ExpenseType.FIXO)
                .map(MonthlyExpense::getAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal eventualTotal = expenses.stream()
                .filter(e -> e.getType() == ExpenseType.EVENTUAL)
                .map(MonthlyExpense::getAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthTotal = fixedTotal.add(eventualTotal);

        // Calcula o rateio — mesmo que será usado por RecipeService ao calcular custo de receitas
        BigDecimal units = settings.getEstimatedMonthlyProductionUnits();
        BigDecimal fixedPerUnit = BigDecimal.ZERO;
        if (units != null && units.compareTo(BigDecimal.ZERO) > 0) {
            fixedPerUnit = fixedTotal.divide(units, 4, RoundingMode.HALF_UP);
        }

        model.addAttribute("currentPage", "gastos-mensais");
        model.addAttribute("pageTitle", "Gastos mensais");
        model.addAttribute("yearMonth", ymStr);
        model.addAttribute("prevMonth", ym.minusMonths(1).toString()); // navegação para mês anterior
        model.addAttribute("nextMonth", ym.plusMonths(1).toString());  // navegação para mês seguinte
        model.addAttribute("expenses", expenses);
        model.addAttribute("fixedTotal", fixedTotal);
        model.addAttribute("eventualTotal", eventualTotal);
        model.addAttribute("monthTotal", monthTotal);
        model.addAttribute("fixedPerProductionUnit", fixedPerUnit);
        model.addAttribute("costSettings", settings);
        model.addAttribute("expenseTypes", ExpenseType.values()); // para o select do formulário
        model.addAttribute("newExpense", monthlyExpenseService.blankExpense(ymStr));
        return "admin/gastos-mensais";
    }

    // POST /admin/gastos-mensais/add — adiciona um gasto e retorna para o mesmo mês
    @PostMapping("/add")
    public String add(@ModelAttribute MonthlyExpense expense,
                      @RequestParam(required = false) String mes) {
        // Garante que o mês esteja preenchido mesmo se o campo hidden vier vazio
        if (expense.getYearMonth() == null || expense.getYearMonth().isBlank()) {
            expense.setYearMonth(monthlyExpenseService.parseMonthOrNow(mes).toString());
        }
        monthlyExpenseRepo.save(expense);
        log.info("Gasto adicionado: {} para o mês {}", expense.getDescription(), expense.getYearMonth());
        return "redirect:/admin/gastos-mensais?mes=" + expense.getYearMonth();
    }

    // POST /admin/gastos-mensais/delete/{id}?mes= — exclui gasto e volta para o mesmo mês
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @RequestParam(required = false) String mes) {
        monthlyExpenseRepo.deleteById(id);
        String redirectMonth = mes != null ? mes : YearMonth.now().toString();
        return "redirect:/admin/gastos-mensais?mes=" + redirectMonth;
    }

    // POST /admin/gastos-mensais/settings — atualiza a quantidade estimada de unidades mensais
    // Afeta diretamente o cálculo de custo das receitas (RecipeService.computeFixedAllocationPerUnit)
    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute CostSettings form,
                               @RequestParam(required = false) String mes) {
        CostSettings existing = costSettingsRepo.findById(CostSettings.SINGLETON_ID)
                .orElseGet(monthlyExpenseService::defaultCostSettings);
        if (form.getEstimatedMonthlyProductionUnits() != null
                && form.getEstimatedMonthlyProductionUnits().compareTo(BigDecimal.ZERO) > 0) {
            existing.setEstimatedMonthlyProductionUnits(form.getEstimatedMonthlyProductionUnits());
        }
        costSettingsRepo.save(existing);
        log.info("Configuração de unidades atualizada: {}", form.getEstimatedMonthlyProductionUnits());
        String m = mes != null ? mes : YearMonth.now().toString();
        return "redirect:/admin/gastos-mensais?mes=" + m;
    }
}
