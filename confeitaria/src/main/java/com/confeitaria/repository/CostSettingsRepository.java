package com.confeitaria.repository;

import com.confeitaria.model.CostSettings;
import org.springframework.data.jpa.repository.JpaRepository;

// Acesso ao singleton de configurações de custo. Sempre há exatamente uma linha (id=1).
// Usado por: RecipeService (lê estimatedMonthlyProductionUnits), MonthlyExpenseController (lê/salva)
public interface CostSettingsRepository extends JpaRepository<CostSettings, Long> {
}
