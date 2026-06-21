package com.example.moneytransfer.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends DomainException {

    private static final String ERROR_CODE = "TRX-400";

    public InsufficientBalanceException(Long id, BigDecimal amount) {
        super(ERROR_CODE, "Insufficient funds on account " + id + " for amount " + amount);
    }

    public InsufficientBalanceException(String message) {
        super(ERROR_CODE, message);
    }
}
