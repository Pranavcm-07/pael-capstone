package com.example.moneytransfer.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table(name = "reward_history")
public class RewardHistory {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "transaction_id", columnDefinition = "char(36)", nullable = false, unique = true)
    private UUID transactionId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Column(name = "created_on")
    private Timestamp createdOn;
}
