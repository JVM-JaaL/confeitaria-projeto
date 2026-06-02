package com.confeitaria.service;

import com.confeitaria.model.CostSettings;
import com.confeitaria.repository.CostSettingsRepository;
import com.confeitaria.repository.MonthlyExpenseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

@Service
@Slf4j
public class RecipeService {

    private final MonthlyExpenseRepository monthlyExpenseRepo;
    private final CostSettingsRepository costSettingsRepo;

    public RecipeService(MonthlyExpenseRepository monthlyExpenseRepo,
                         CostSettingsRepository costSettingsRepo) {
        this.monthlyExpenseRepo = monthlyExpenseRepo;
        this.costSettingsRepo = costSettingsRepo;
    }

    public BigDecimal computeFixedAllocationPerUnit(String yearMonth) {
        BigDecimal monthlyFixed = monthlyExpenseRepo.sumAmountByYearMonth(yearMonth);
        CostSettings settings = costSettingsRepo.findById(CostSettings.SINGLETON_ID).orElse(null);
        if (settings == null || settings.getEstimatedMonthlyProductionUnits() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal units = settings.getEstimatedMonthlyProductionUnits();
        if (units.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = monthlyFixed.divide(units, 4, RoundingMode.HALF_UP);
        log.debug("Rateio fixo/unidade para {}: {}", yearMonth, result);
        return result;
    }

    public YearMonth parseYearMonth(String mes) {
        if (mes == null || mes.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(mes);
        } catch (Exception e) {
            log.warn("Mês inválido '{}', usando mês atual", mes);
            return YearMonth.now();
        }
    }

    public BigDecimal getMonthlyFixed(String yearMonth) {
        return monthlyExpenseRepo.sumAmountByYearMonth(yearMonth);
    }

    public BigDecimal getEstimatedUnits() {
        CostSettings settings = costSettingsRepo.findById(CostSettings.SINGLETON_ID).orElse(null);
        if (settings == null || settings.getEstimatedMonthlyProductionUnits() == null) {
            return BigDecimal.ZERO;
        }
        return settings.getEstimatedMonthlyProductionUnits();
    }
}
