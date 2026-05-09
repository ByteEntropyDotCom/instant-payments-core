package com.byteentropy.instant_payments_core.event;
import java.math.BigDecimal;

public record PaymentResultEvent(String transactionId, String status, String message, BigDecimal amount) {}