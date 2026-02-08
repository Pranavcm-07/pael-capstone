package com.example.moneytransfer.exception;

public class AccountNotFoundException extends RuntimeException {

    private final Long accountId;

    public AccountNotFoundException(Long accountId) {
        super(String.format("Account with ID %d not found", accountId));
        this.accountId = accountId;
    }

    public AccountNotFoundException(String message) {
        super(message);
        this.accountId = null;
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.accountId = null;
    }

    public Long getAccountId() {
        return accountId;
    }
}
