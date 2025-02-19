package org.example.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID fileId;  //  Tracks which file this transaction belongs to

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private BigDecimal amount;  //  Updated field (Renamed from `grossSalesAmount`)

    @Column(nullable = false)
    private String customerName;  //  New column

    @Column(nullable = false)
    private String paymentMethod;  //  New column

    @Column(nullable = false)
    private String shippingAddressCity;  //  New column

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

    public UUID getId() {
        return id;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getShippingAddressCity() {
        return shippingAddressCity;
    }
}
