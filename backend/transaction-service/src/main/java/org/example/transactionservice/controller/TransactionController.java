package org.example.transactionservice.controller;

import org.example.transactionservice.model.Transaction;
import org.example.transactionservice.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<Transaction>> getTransactionsByFileId(@PathVariable UUID fileId) {
        List<Transaction> transactions = transactionRepository.findByFileId(fileId);
        return transactions.isEmpty() ? ResponseEntity.badRequest().body(List.of()) : ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransactionById(@PathVariable String transactionId) {
        Optional<Transaction> transaction = transactionRepository.findByTransactionId(transactionId);
        return transaction.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(400).body(Map.of("error", "Transaction not found.")));
    }


}
