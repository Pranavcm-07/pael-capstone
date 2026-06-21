package com.example.moneytransfer.domain.entity;

import com.example.moneytransfer.domain.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "account_status_audit")
@Data
@NoArgsConstructor
public class AccountStatusAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private AccountStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private AccountStatus newStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "created_on")
    private Timestamp createdOn;

    public AccountStatusAudit(Long accountId, AccountStatus oldStatus, AccountStatus newStatus, String reason, String changedBy) {
        this.accountId = accountId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
        this.createdOn = Timestamp.from(Instant.now());
    }
}
