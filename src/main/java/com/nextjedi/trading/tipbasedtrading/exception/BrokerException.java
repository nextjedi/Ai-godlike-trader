package com.nextjedi.trading.tipbasedtrading.exception;

public class BrokerException extends TradingException {
    public BrokerException(String message) {
        super(message, "BROKER_ERROR");
    }

    public BrokerException(String message, Throwable cause) {
        super(message, "BROKER_ERROR", cause);
    }
}
