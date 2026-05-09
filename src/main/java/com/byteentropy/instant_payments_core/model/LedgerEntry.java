package com.byteentropy.instant_payments_core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class LedgerEntry {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;
    
    private String transactionId;
    private BigDecimal amount;

    public LedgerEntry() {}
    
    public LedgerEntry(String transactionId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.amount = amount;
    }
    
    public String getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
}