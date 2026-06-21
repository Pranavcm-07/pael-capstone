package com.example.moneytransfer.exception;

public class AccountNotFoundException extends DomainException {

    public AccountNotFoundException(Long id) {
        super("ACC-404", "Account not found: " + id);
    }
}

