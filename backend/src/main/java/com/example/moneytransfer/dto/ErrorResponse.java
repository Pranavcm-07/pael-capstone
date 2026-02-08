package com.example.moneytransfer.dto;

public record ErrorResponse(String errorCode, String message) {
    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
    public static final String ACCOUNT_NOT_ACTIVE = "ACCOUNT_NOT_ACTIVE";
    public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
    public static final String DUPLICATE_TRANSFER = "DUPLICATE_TRANSFER";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    public static ErrorResponse accountNotFound(Long accountId) {
        return new ErrorResponse(ACCOUNT_NOT_FOUND, String.format("Account with ID %d not found", accountId));
    }

    public static ErrorResponse accountNotActive(Long accountId, String status) {
        return new ErrorResponse(ACCOUNT_NOT_ACTIVE,
                String.format("Account %d is not active. Current status: %s", accountId, status));
    }

    public static ErrorResponse insufficientBalance() {
        return new ErrorResponse(INSUFFICIENT_BALANCE, "Insufficient balance in the source account");
    }

    public static ErrorResponse duplicateTransfer(String idempotencyKey) {
        return new ErrorResponse(DUPLICATE_TRANSFER,
                String.format("Duplicate transfer detected with idempotency key: %s", idempotencyKey));
    }

    public static ErrorResponse validationError(String message) {
        return new ErrorResponse(VALIDATION_ERROR, message);
    }

    public static ErrorResponse internalError(String message) {
        return new ErrorResponse(INTERNAL_ERROR, message);
    }
}
