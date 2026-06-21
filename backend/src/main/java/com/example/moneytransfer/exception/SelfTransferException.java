package com.example.moneytransfer.exception;

public class SelfTransferException extends DomainException {
    public SelfTransferException(String message) {
        super("TRX-400", message);
    }
}
