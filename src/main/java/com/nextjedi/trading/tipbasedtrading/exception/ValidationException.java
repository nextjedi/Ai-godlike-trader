package com.nextjedi.trading.tipbasedtrading.exception;

public class ValidationException extends TradingException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
}
