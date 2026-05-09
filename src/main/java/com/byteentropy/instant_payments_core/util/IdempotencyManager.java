package com.byteentropy.instant_payments_core.util;

import com.byteentropy.instant_payments_core.repository.LedgerRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IdempotencyManager {

    private final LedgerRepository repository;

    public IdempotencyManager(LedgerRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean isUnique(String transactionId) {
        // Checks the actual database instead of an in-memory Set
        return repository.findByTransactionId(transactionId).isEmpty();
    }
}