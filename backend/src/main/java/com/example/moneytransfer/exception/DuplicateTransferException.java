package com.example.moneytransfer.exception;

public class DuplicateTransferException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateTransferException(String idempotencyKey) {
        super(String.format("Duplicate transfer detected with idempotency key: %s",
                idempotencyKey));
        this.idempotencyKey = idempotencyKey;
    }

    public DuplicateTransferException(String message, boolean isCustomMessage) {
        super(message);
        this.idempotencyKey = null;
    }

    public DuplicateTransferException(String message, Throwable cause) {
        super(message, cause);
        this.idempotencyKey = null;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
