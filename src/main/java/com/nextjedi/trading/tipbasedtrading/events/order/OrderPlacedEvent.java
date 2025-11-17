package com.nextjedi.trading.tipbasedtrading.events.order;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when an order is placed with the broker
 */
@Getter
public class OrderPlacedEvent extends DomainEvent {
    private final String orderId;
    private final String tradeId;
    private final String brokerName;
    private final Long instrumentToken;
    private final String tradingSymbol;
    private final String transactionType; // BUY, SELL
    private final String orderType; // MARKET, LIMIT, SL, SL-M
    private final Integer quantity;
    private final Double price;
    private final Double triggerPrice;
    private final String product; // MIS, NRML, CNC

    @Builder
    public OrderPlacedEvent(String orderId, String tradeId, String brokerName,
                           Long instrumentToken, String tradingSymbol,
                           String transactionType, String orderType,
                           Integer quantity, Double price, Double triggerPrice,
                           String product) {
        super();
        this.orderId = orderId;
        this.tradeId = tradeId;
        this.brokerName = brokerName;
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.transactionType = transactionType;
        this.orderType = orderType;
        this.quantity = quantity;
        this.price = price;
        this.triggerPrice = triggerPrice;
        this.product = product;
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }
}
