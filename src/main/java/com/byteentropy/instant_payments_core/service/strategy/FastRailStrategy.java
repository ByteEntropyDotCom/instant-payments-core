package com.byteentropy.instant_payments_core.service.strategy;

import com.byteentropy.instant_payments_core.model.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class FastRailStrategy implements PaymentRailStrategy {

    @Override 
    public boolean process(PaymentRequest r) { 
        // Mocked implementation: logic for external API call removed for clarity
        return true; 
    }

    @Override 
    public String getRailName() { return "FAST"; }
}