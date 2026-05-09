package com.byteentropy.instant_payments_core.service;

import com.byteentropy.instant_payments_core.model.LedgerEntry;
import com.byteentropy.instant_payments_core.repository.LedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class LedgerService {
    private final LedgerRepository repository;

    public LedgerService(LedgerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void recordTransaction(String txId, BigDecimal amount) {
        // IDEMPOTENCY CHECK: Only save if it doesn't already exist
        if (repository.findByTransactionId(txId).isEmpty()) {
            repository.save(new LedgerEntry(txId, amount));
        } else {
            System.out.println("DEBUG: Ignoring duplicate transaction: " + txId);
        }
    }
}