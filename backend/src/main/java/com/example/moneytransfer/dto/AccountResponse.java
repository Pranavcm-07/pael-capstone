package com.example.moneytransfer.dto;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountResponse(Long id, String holderName, BigDecimal balance, AccountStatus status) {
    public static AccountResponse fromAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        return new AccountResponse(account.getId(), account.getHolderName(), account.getBalance(), account.getStatus());
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "Unknown";
    }
}
