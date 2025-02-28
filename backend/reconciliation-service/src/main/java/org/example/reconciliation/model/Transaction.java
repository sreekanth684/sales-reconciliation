package org.example.reconciliation.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")  // Ensure this matches the table name in Transaction Service
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID fileId;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String shippingAddressCity;

    public Transaction() {}

    public Transaction(UUID fileId, String transactionId, LocalDate transactionDate, BigDecimal amount,
                       String customerName, String paymentMethod, String shippingAddressCity) {
        this.fileId = fileId;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.customerName = customerName;
        this.paymentMethod = paymentMethod;
        this.shippingAddressCity = shippingAddressCity;
    }

    public UUID getId() { return id; }
    public UUID getFileId() { return fileId; }
    public String getTransactionId() { return transactionId; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public BigDecimal getAmount() { return amount; }
    public String getCustomerName() { return customerName; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getShippingAddressCity() { return shippingAddressCity; }
}
