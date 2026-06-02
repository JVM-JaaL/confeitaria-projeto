package com.confeitaria.repository;

import com.confeitaria.model.MonthlyExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MonthlyExpenseRepository extends JpaRepository<MonthlyExpense, Long> {

    List<MonthlyExpense> findByYearMonthOrderByIdDesc(String yearMonth);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM MonthlyExpense e WHERE e.yearMonth = :ym")
    BigDecimal sumAmountByYearMonth(@Param("ym") String yearMonth);
}
