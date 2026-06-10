package com.confeitaria.repository;

import com.confeitaria.model.MonthlyExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

// Acesso ao banco para MonthlyExpense.
// Usado por: MonthlyExpenseController (listagem e CRUD), RecipeService (soma de gastos fixos para rateio)
public interface MonthlyExpenseRepository extends JpaRepository<MonthlyExpense, Long> {

    // Lista todos os gastos de um mês específico (ex: "2026-05"), do mais recente para o mais antigo
    List<MonthlyExpense> findByYearMonthOrderByIdDesc(String yearMonth);

    // Soma todos os gastos (FIXO + EVENTUAL) de um mês — usado por RecipeService para calcular rateio
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM MonthlyExpense e WHERE e.yearMonth = :ym")
    BigDecimal sumAmountByYearMonth(@Param("ym") String yearMonth);
}
