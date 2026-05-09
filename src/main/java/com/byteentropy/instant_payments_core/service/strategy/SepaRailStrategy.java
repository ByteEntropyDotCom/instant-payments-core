package com.byteentropy.instant_payments_core.service.strategy;
import com.byteentropy.instant_payments_core.model.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class SepaRailStrategy implements PaymentRailStrategy {
    @Override public boolean process(PaymentRequest r) { return true; }
    @Override public String getRailName() { return "SEPA"; }
}