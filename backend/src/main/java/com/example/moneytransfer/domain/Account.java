package com.example.moneytransfer.domain;

import com.example.moneytransfer.domain.enums.AccountStatus;
import com.example.moneytransfer.exception.AccountNotActiveException;
import com.example.moneytransfer.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Account {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Version
    private Integer version;

    @Column(name = "last_updated")
    private Timestamp lastUpdated;

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public void debit(BigDecimal amount) {
        if (!isActive()) {
            throw new AccountNotActiveException("Account is " + this.status);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance == null || this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient funds for transfer");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (!isActive()) {
            throw new AccountNotActiveException("Account is " + this.status);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        this.balance = this.balance.add(amount);
    }
}
