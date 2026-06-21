package com.example.moneytransfer.exception;

public class UnauthorizedAccessException extends DomainException {
    public UnauthorizedAccessException(String message) {
        super("ACC-403", message);
    }
}
