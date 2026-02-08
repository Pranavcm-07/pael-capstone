package com.example.moneytransfer.domain;

import com.example.moneytransfer.enums.AccountStatus;
import com.example.moneytransfer.exception.AccountNotActiveException;
import com.example.moneytransfer.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Account {

    private Long id;
    private String holderName;
    private BigDecimal balance;
    private AccountStatus status;
    private Integer version;
    private Instant lastUpdated;

    public Account() {
        this.balance = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
        this.version = 0;
        this.lastUpdated = Instant.now();
    }

    public Account(Long id, String holderName, BigDecimal balance, AccountStatus status) {
        this.id = id;
        this.holderName = holderName;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.status = status != null ? status : AccountStatus.ACTIVE;
        this.version = 0;
        this.lastUpdated = Instant.now();
    }

    public void debit(BigDecimal amount) {
        validateAmount(amount);
        validateAccountIsActive();
        validateSufficientBalance(amount);

        this.balance = this.balance.subtract(amount);
        updateTimestamp();
    }

    public void credit(BigDecimal amount) {
        validateAmount(amount);
        validateAccountIsActive();

        this.balance = this.balance.add(amount);
        updateTimestamp();
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    private void validateAccountIsActive() {
        if (!isActive()) {
            throw new AccountNotActiveException(this.id, this.status);
        }
    }

    private void validateSufficientBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(this.id, this.balance, amount);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private void updateTimestamp() {
        this.lastUpdated = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
        updateTimestamp();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        updateTimestamp();
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
        updateTimestamp();
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void incrementVersion() {
        this.version = (this.version == null ? 0 : this.version) + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", holderName='" + holderName + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                ", version=" + version +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
