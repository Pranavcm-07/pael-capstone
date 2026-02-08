package com.example.moneytransfer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResponse(UUID transactionId, String status, String message, Long debitedFrom, Long creditedTo,
        BigDecimal amount) {
    public static TransferResponse success(UUID transactionId, Long debitedFrom, Long creditedTo, BigDecimal amount) {
        return new TransferResponse(transactionId, "SUCCESS", "Transfer completed successfully", debitedFrom,
                creditedTo, amount);
    }

    public static TransferResponse failed(UUID transactionId, String message, Long debitedFrom, Long creditedTo,
            BigDecimal amount) {
        return new TransferResponse(transactionId, "FAILED", message, debitedFrom, creditedTo, amount);
    }

    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }
}
