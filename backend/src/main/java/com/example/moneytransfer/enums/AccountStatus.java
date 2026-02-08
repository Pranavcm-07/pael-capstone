package com.example.moneytransfer.enums;

public enum AccountStatus {

    ACTIVE("Active", "Account is operational"),

    LOCKED("Locked", "Account is temporarily suspended"),

    CLOSED("Closed", "Account is permanently closed");

    private final String displayName;
    private final String description;

    AccountStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean allowsTransactions() {
        return this == ACTIVE;
    }
}
