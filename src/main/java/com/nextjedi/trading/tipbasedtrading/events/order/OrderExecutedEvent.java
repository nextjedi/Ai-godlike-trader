package com.nextjedi.trading.tipbasedtrading.events.order;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when an order is successfully executed by the broker
 */
@Getter
public class OrderExecutedEvent extends DomainEvent {
    private final String orderId;
    private final String tradeId;
    private final String brokerName;
    private final Long instrumentToken;
    private final String tradingSymbol;
    private final String transactionType;
    private final Integer quantity;
    private final Double averagePrice;
    private final Double totalValue;
    private final String orderStatus; // COMPLETE

    @Builder
    public OrderExecutedEvent(String orderId, String tradeId, String brokerName,
                             Long instrumentToken, String tradingSymbol,
                             String transactionType, Integer quantity,
                             Double averagePrice, Double totalValue,
                             String orderStatus) {
        super();
        this.orderId = orderId;
        this.tradeId = tradeId;
        this.brokerName = brokerName;
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.totalValue = totalValue;
        this.orderStatus = orderStatus;
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }
}
