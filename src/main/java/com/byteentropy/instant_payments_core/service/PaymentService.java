package com.byteentropy.instant_payments_core.service;

import com.byteentropy.instant_payments_core.model.*;
import com.byteentropy.instant_payments_core.service.strategy.PaymentRailStrategy;
import com.byteentropy.instant_payments_core.util.IdempotencyManager;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final Map<String, PaymentRailStrategy> strategies;
    private final IdempotencyManager idempotencyManager;

    public PaymentService(List<PaymentRailStrategy> list, IdempotencyManager im) {
        this.strategies = list.stream().collect(Collectors.toMap(s -> s.getRailName().toUpperCase(), Function.identity()));
        this.idempotencyManager = im;
    }

    public void processPayment(PaymentRequest request) {
        // 1. Idempotency Check
        if (!idempotencyManager.isUnique(request.transactionId())) {
            System.out.println("DEBUG: Duplicate transaction ignored: " + request.transactionId());
            return;
        }
        
        // 2. Find Strategy
        PaymentRailStrategy strategy = strategies.get(request.rail().toUpperCase());
        if (strategy == null) {
            System.err.println("ERROR: No strategy found for rail: " + request.rail());
            return;
        }

        // 3. Dispatch to Rail Topic
        strategy.dispatch(request);
        System.out.println("DEBUG: Transaction " + request.transactionId() + " dispatched to " + request.rail());
    }
}