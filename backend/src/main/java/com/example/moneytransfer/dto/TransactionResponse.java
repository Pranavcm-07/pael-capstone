package com.example.moneytransfer.dto;

import com.example.moneytransfer.domain.TransactionLog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(UUID id, Long fromAccountId, Long toAccountId, BigDecimal amount,
        String status, String failureReason, Instant createdOn) {

    public static TransactionResponse fromTransactionLog(TransactionLog log) {
        return new TransactionResponse(
                log.getId(),
                log.getFromAccountId(),
                log.getToAccountId(),
                log.getAmount(),
                log.getStatus() != null ? log.getStatus().name() : null,
                log.getFailureReason(),
                log.getCreatedOn()
        );
    }
}
