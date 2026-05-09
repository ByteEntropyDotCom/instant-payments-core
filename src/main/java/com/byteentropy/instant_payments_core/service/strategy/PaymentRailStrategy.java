package com.byteentropy.instant_payments_core.service.strategy;
import com.byteentropy.instant_payments_core.model.PaymentRequest;

public interface PaymentRailStrategy {
    boolean process(PaymentRequest request);
    String getRailName();
}