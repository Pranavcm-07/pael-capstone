package com.example.moneytransfer.exception;

public class AccountNotActiveException extends DomainException {

    public AccountNotActiveException(String id) {
        super("ACC-403", "Account not active: " + id);
    }
}

