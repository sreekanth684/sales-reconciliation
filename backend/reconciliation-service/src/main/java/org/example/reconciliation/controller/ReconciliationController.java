package org.example.reconciliation.controller;

import org.example.reconciliation.model.Transaction;
import org.example.reconciliation.service.ReconciliationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<Transaction>> getTransactionsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        Page<Transaction> transactions = reconciliationService.fetchTransactionsForPeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/gross-sales")
    public ResponseEntity<Map<String, Object>> getTotalGrossSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal totalSales = reconciliationService.getTotalGrossSalesAmount(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "totalGrossSales", totalSales
        ));
    }

    @GetMapping("/sales-tax")
    public ResponseEntity<Map<String, Object>> getTotalSalesTax(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal totalSalesTax = reconciliationService.getTotalSalesTaxAmount(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "totalSalesTax", totalSalesTax
        ));
    }

    @GetMapping("/totals-by-city")
    public ResponseEntity<Page<Map<String, Object>>> getTotalsByCity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        Page<Map<String, Object>> totalsByCity = reconciliationService.getAggregatedTotalsByCity(startDate, endDate, pageable);
        return ResponseEntity.ok(totalsByCity);
    }
}
