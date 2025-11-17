package com.nextjedi.trading.tipbasedtrading.broker.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response object for order placement/modification
 */
@Data
@Builder
public class OrderResponse {
    private String orderId;
    private boolean success;
    private String message;
    private String errorCode;
}
