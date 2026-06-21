package com.example.moneytransfer.domain.entity;

import com.example.moneytransfer.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table(name = "transaction_logs")
public class TransactionLog {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "from_account")
    private Long fromAccountId;

    @Column(name = "to_account")
    private Long toAccountId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_on")
    private Timestamp createdOn;
}
