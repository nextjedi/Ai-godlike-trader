package com.nextjedi.trading.tipbasedtrading.broker.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Detailed information about an order
 */
@Data
@Builder
public class OrderDetail {
    private String orderId;
    private String parentOrderId;
    private String exchange;
    private String tradingSymbol;
    private Long instrumentToken;
    private OrderRequest.TransactionType transactionType;
    private OrderRequest.OrderType orderType;
    private OrderRequest.Product product;
    private Integer quantity;
    private Integer filledQuantity;
    private Integer pendingQuantity;
    private Double price;
    private Double triggerPrice;
    private Double averagePrice;
    private OrderStatus status;
    private String statusMessage;
    private Instant orderTimestamp;
    private Instant exchangeTimestamp;
    private String tag;

    public enum OrderStatus {
        OPEN,           // Order is pending execution
        COMPLETE,       // Order is fully executed
        CANCELLED,      // Order is cancelled
        REJECTED,       // Order is rejected
        MODIFY_PENDING, // Modification is pending
        CANCEL_PENDING, // Cancellation is pending
        TRIGGER_PENDING // SL order waiting for trigger
    }
}
