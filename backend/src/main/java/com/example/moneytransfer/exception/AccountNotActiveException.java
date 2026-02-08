package com.example.moneytransfer.exception;

import com.example.moneytransfer.enums.AccountStatus;

public class AccountNotActiveException extends RuntimeException {

    private final Long accountId;
    private final AccountStatus currentStatus;

    public AccountNotActiveException(Long accountId, AccountStatus currentStatus) {
        super(String.format("Account %d is not active. Current status: %s",
                accountId, currentStatus.getDisplayName()));
        this.accountId = accountId;
        this.currentStatus = currentStatus;
    }

    public AccountNotActiveException(String message) {
        super(message);
        this.accountId = null;
        this.currentStatus = null;
    }

    public AccountNotActiveException(String message, Throwable cause) {
        super(message, cause);
        this.accountId = null;
        this.currentStatus = null;
    }

    public Long getAccountId() {
        return accountId;
    }

    public AccountStatus getCurrentStatus() {
        return currentStatus;
    }
}
