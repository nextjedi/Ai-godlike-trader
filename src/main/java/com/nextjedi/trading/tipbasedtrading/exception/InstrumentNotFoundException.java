package com.nextjedi.trading.tipbasedtrading.exception;

public class InstrumentNotFoundException extends RuntimeException{
    public InstrumentNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
