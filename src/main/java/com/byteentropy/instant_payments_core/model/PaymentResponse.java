package com.byteentropy.instant_payments_core.model;

public record PaymentResponse(String transactionId, String status, String message) {}