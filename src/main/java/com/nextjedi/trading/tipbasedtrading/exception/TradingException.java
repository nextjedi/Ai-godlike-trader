package com.nextjedi.trading.tipbasedtrading.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TradingException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> metadata;

    public TradingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.metadata = new HashMap<>();
    }

    public TradingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.metadata = new HashMap<>();
    }

    public TradingException addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
}
