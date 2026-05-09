package com.byteentropy.instant_payments_core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaymentRequest(
    @JsonProperty("transactionId") @NotBlank String transactionId,
    @JsonProperty("rail") @NotBlank String rail,
    @JsonProperty("sender") @NotBlank String sender,
    @JsonProperty("receiver") @NotBlank String receiver,
    @JsonProperty("amount") @NotNull @Positive BigDecimal amount
) {}