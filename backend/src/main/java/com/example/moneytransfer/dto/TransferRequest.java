package com.example.moneytransfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(@NotNull(message = "Source account ID is required") Long fromAccountId,

        @NotNull(message = "Destination account ID is required") Long toAccountId,

        @NotNull(message = "Transfer amount is required") @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01") BigDecimal amount,

        @NotNull(message = "Idempotency key is required") String idempotencyKey) {
    public static TransferRequest of(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        return new TransferRequest(fromAccountId, toAccountId, amount, java.util.UUID.randomUUID().toString());
    }
}
