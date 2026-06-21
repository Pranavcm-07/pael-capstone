package com.example.moneytransfer.dto;

import java.math.BigDecimal;

public class TransferResponse {

    private String transactionId;
    private String status;
    private String message;
    private long debitedFrom;
    private long creditedTo;
    private BigDecimal amount;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDebitedFrom() {
        return debitedFrom;
    }

    public void setDebitedFrom(Long debitedFrom) {
        this.debitedFrom = debitedFrom;
    }

    public Long getCreditedTo() {
        return creditedTo;
    }

    public void setCreditedTo(Long creditedTo) {
        this.creditedTo = creditedTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    private Integer rewardPointsEarned;

    public Integer getRewardPointsEarned() {
        return rewardPointsEarned;
    }

    public void setRewardPointsEarned(Integer rewardPointsEarned) {
        this.rewardPointsEarned = rewardPointsEarned;
    }
}

