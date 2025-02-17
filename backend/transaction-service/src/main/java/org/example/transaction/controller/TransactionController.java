package com.example.transaction.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @PostMapping
    public ResponseEntity<String> saveTransaction(@RequestBody String transaction) {
        // TODO: Implement database save logic
        return ResponseEntity.ok("Transaction saved successfully!");
    }
}
