package com.byteentropy.instant_payments_core.event;
import java.math.BigDecimal;

public record PaymentEvent(String transactionId, String rail, String sender, String receiver, BigDecimal amount) {}