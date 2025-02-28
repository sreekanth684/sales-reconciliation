package org.example.reconciliation.service;

import org.example.reconciliation.model.Transaction;
import org.example.reconciliation.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReconciliationService {

    private final TransactionRepository transactionRepository;

    public ReconciliationService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get paginated transactions for a given period.
     */
    public Page<Transaction> fetchTransactionsForPeriod(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate, pageable);
    }

    /**
     * Get total gross sales amount using SQL aggregation.
     */
    public BigDecimal getTotalGrossSalesAmount(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalGrossSalesAmount(startDate, endDate);
    }

    /**
     * Get total sales tax amount using SQL aggregation.
     */
    public BigDecimal getTotalSalesTaxAmount(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalSalesTaxAmount(startDate, endDate);
    }

    /**
     * Get paginated aggregated totals by city using SQL aggregation.
     */
    public Page<Map<String, Object>> getAggregatedTotalsByCity(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return transactionRepository.getAggregatedTotalsByCity(startDate, endDate, pageable);
    }
}
