package com.nextjedi.trading.tipbasedtrading.events.order;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when an order is cancelled
 */
@Getter
public class OrderCancelledEvent extends DomainEvent {
    private final String orderId;
    private final String tradeId;
    private final String brokerName;
    private final String reason;

    @Builder
    public OrderCancelledEvent(String orderId, String tradeId, String brokerName, String reason) {
        super();
        this.orderId = orderId;
        this.tradeId = tradeId;
        this.brokerName = brokerName;
        this.reason = reason;
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }
}
