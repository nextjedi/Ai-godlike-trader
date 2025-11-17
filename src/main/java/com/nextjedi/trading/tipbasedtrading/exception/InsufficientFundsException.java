package com.nextjedi.trading.tipbasedtrading.exception;

public class InsufficientFundsException extends TradingException {
    public InsufficientFundsException(String message, Double required, Double available) {
        super(message, "INSUFFICIENT_FUNDS");
        addMetadata("required", required);
        addMetadata("available", available);
    }
}
