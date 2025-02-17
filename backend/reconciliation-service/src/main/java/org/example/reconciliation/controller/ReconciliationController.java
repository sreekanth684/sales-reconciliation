package com.example.reconciliation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reports")
public class ReconciliationController {

    @GetMapping("/total-sales")
    public ResponseEntity<BigDecimal> getTotalSales(@RequestParam String startDate, @RequestParam String endDate) {
        // TODO: Implement sales calculation logic
        return ResponseEntity.ok(new BigDecimal("10000.00")); // Mock data
    }
}
