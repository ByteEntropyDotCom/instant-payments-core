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

    public PaymentResponse processPayment(PaymentRequest request) {
        if (!idempotencyManager.isUnique(request.transactionId())) 
            return new PaymentResponse(request.transactionId(), "DUPLICATE", "Skipped");
        
        PaymentRailStrategy strategy = strategies.get(request.rail().toUpperCase());
        if (strategy == null) return new PaymentResponse(request.transactionId(), "FAILED", "No Strategy");

        return strategy.process(request) 
            ? new PaymentResponse(request.transactionId(), "SUCCESS", "OK")
            : new PaymentResponse(request.transactionId(), "FAILED", "Rail Error");
    }
}