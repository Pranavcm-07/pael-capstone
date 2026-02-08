package com.example.moneytransfer.enums;

public enum TransactionStatus {

    SUCCESS("Success", "Transaction completed successfully"),

    FAILED("Failed", "Transaction failed");

    private final String displayName;
    private final String description;

    TransactionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccessful() {
        return this == SUCCESS;
    }
}
