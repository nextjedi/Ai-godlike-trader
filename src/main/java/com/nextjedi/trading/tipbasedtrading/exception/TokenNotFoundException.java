package com.nextjedi.trading.tipbasedtrading.exception;

public class TokenNotFoundException extends RuntimeException{
    public TokenNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
