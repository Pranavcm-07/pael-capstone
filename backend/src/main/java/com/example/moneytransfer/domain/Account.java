package com.example.moneytransfer.domain;

import com.example.moneytransfer.enums.AccountStatus;
import com.example.moneytransfer.exception.AccountNotActiveException;
import com.example.moneytransfer.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "ACCOUNTS")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holder_name", nullable = false, length = 255)
    private String holderName;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "last_updated")
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

    @PreUpdate
    public void setLastUpdatedOnSave() {
        this.lastUpdated = Instant.now();
    }

    public void debit(BigDecimal amount) {
        validateAmount(amount);
        validateAccountIsActive();
        validateSufficientBalance(amount);

        this.balance = this.balance.subtract(amount);
        this.lastUpdated = Instant.now();
    }

    public void credit(BigDecimal amount) {
        validateAmount(amount);
        validateAccountIsActive();

        this.balance = this.balance.add(amount);
        this.lastUpdated = Instant.now();
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
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
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
