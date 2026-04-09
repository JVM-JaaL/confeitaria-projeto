package com.confeitaria.repository;
import com.confeitaria.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findAllByOrderBySaleDateDesc();
    List<Sale> findBySaleDateBetweenOrderBySaleDateAsc(LocalDate start, LocalDate end);

    @Query("SELECT s.productGroup, SUM(s.cost), SUM(s.revenue), SUM(s.revenue - s.cost) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end GROUP BY s.productGroup")
    List<Object[]> groupSummaryByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s.productName, SUM(s.cost), SUM(s.revenue), SUM(s.revenue - s.cost) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end GROUP BY s.productName")
    List<Object[]> productSummaryByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s.saleDate, SUM(s.cost), SUM(s.revenue), SUM(s.revenue - s.cost) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end GROUP BY s.saleDate ORDER BY s.saleDate")
    List<Object[]> dailySummaryByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
