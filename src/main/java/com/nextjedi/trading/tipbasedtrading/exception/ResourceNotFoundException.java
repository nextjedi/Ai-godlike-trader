package com.nextjedi.trading.tipbasedtrading.exception;

public class ResourceNotFoundException extends TradingException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier), "RESOURCE_NOT_FOUND");
        addMetadata("resourceType", resourceType);
        addMetadata("identifier", identifier);
    }
}
