package com.confeitaria.repository;

import com.confeitaria.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

// Acesso ao banco para Sale. Tem queries analíticas que alimentam gráficos e tabelas de resumo.
// Usado por: SaleController (CRUD + gráficos de vendas), FinancialDashboardController (relatório financeiro),
//            AdminController (contagem no dashboard)
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Todas as vendas da mais recente para a mais antiga — tabela completa em /admin/vendas
    List<Sale> findAllByOrderBySaleDateDesc();

    // Vendas dentro de um período, em ordem cronológica — base dos gráficos e KPIs filtrados
    List<Sale> findBySaleDateBetweenOrderBySaleDateAsc(LocalDate start, LocalDate end);

    // Agrega por grupo de produto no período: [grupo, custo, receita, lucro] — gráfico de pizza e tabela
    @Query("SELECT s.productGroup, SUM(s.cost), SUM(s.revenue), SUM(s.revenue - s.cost) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end GROUP BY s.productGroup")
    List<Object[]> groupSummaryByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Agrega por nome de produto no período: [produto, custo, receita, lucro] — tabela de resumo
    @Query("SELECT s.productName, SUM(s.cost), SUM(s.revenue), SUM(s.revenue - s.cost) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end GROUP BY s.productName")
    List<Object[]> productSummaryByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Agrega por data no período: [data, custo, receita, lucro] — gráfico de linha diário
    @Query("SELECT s.saleDate, SUM(s.cost), SUM(s.revenue), SUM(s.revenue - s.cost) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end GROUP BY s.saleDate ORDER BY s.saleDate")
    List<Object[]> dailySummaryByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
