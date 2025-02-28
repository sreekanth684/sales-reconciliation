package org.example.reconciliation.repository;

import org.example.reconciliation.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Fetch paginated transactions within a date range
    Page<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Get total gross sales amount for a given period
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalGrossSalesAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total sales tax amount for a given period
    @Query("SELECT COALESCE(SUM(t.amount * 0.1), 0) FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesTaxAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get aggregated totals by city with pagination
    @Query("SELECT t.shippingAddressCity as city, COALESCE(SUM(t.amount), 0) as totalSales " +
            "FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.shippingAddressCity")
    Page<Map<String, Object>> getAggregatedTotalsByCity(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        Pageable pageable);
}
