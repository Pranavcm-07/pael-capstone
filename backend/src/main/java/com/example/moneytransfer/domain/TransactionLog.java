package com.example.moneytransfer.domain;

import com.example.moneytransfer.enums.TransactionStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TRANSACTION_LOGS", indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_from_account_id", columnList = "from_account_id"),
        @Index(name = "idx_to_account_id", columnList = "to_account_id")
})
public class TransactionLog {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)", updatable = false)
    private UUID id;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdOn == null) {
            this.createdOn = Instant.now();
        }
    }

    public TransactionLog() {
        this.id = UUID.randomUUID();
        this.createdOn = Instant.now();
    }

    public TransactionLog(Long fromAccountId, Long toAccountId,
            BigDecimal amount, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.createdOn = Instant.now();
    }

    public static TransactionLog success(Long fromAccountId, Long toAccountId,
            BigDecimal amount, String idempotencyKey) {
        TransactionLog log = new TransactionLog(fromAccountId, toAccountId,
                amount, idempotencyKey);
        log.setStatus(TransactionStatus.SUCCESS);
        return log;
    }

    public static TransactionLog failed(Long fromAccountId, Long toAccountId,
            BigDecimal amount, String idempotencyKey,
            String failureReason) {
        TransactionLog log = new TransactionLog(fromAccountId, toAccountId,
                amount, idempotencyKey);
        log.setStatus(TransactionStatus.FAILED);
        log.setFailureReason(failureReason);
        return log;
    }

    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
        this.failureReason = null;
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    public boolean isSuccessful() {
        return this.status == TransactionStatus.SUCCESS;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TransactionLog that = (TransactionLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TransactionLog{" +
                "id=" + id +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", amount=" + amount +
                ", status=" + status +
                ", failureReason='" + failureReason + '\'' +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                ", createdOn=" + createdOn +
                '}';
    }
}
