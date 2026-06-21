package com.example.moneytransfer.exception;

public class DuplicateTransferException extends DomainException {

    public DuplicateTransferException(String idempotencyKey) {
        super("TRX-409", "Duplicate transfer for idempotency key: " + idempotencyKey);
    }
}

