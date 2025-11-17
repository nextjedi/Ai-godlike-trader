package com.nextjedi.trading.tipbasedtrading.events.order;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when an order is modified (e.g., trailing stop-loss)
 */
@Getter
public class OrderModifiedEvent extends DomainEvent {
    private final String orderId;
    private final String tradeId;
    private final String brokerName;
    private final Double oldPrice;
    private final Double newPrice;
    private final Double oldTriggerPrice;
    private final Double newTriggerPrice;
    private final String reason; // TRAILING_STOP_LOSS, USER_MODIFICATION

    @Builder
    public OrderModifiedEvent(String orderId, String tradeId, String brokerName,
                             Double oldPrice, Double newPrice,
                             Double oldTriggerPrice, Double newTriggerPrice,
                             String reason) {
        super();
        this.orderId = orderId;
        this.tradeId = tradeId;
        this.brokerName = brokerName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.oldTriggerPrice = oldTriggerPrice;
        this.newTriggerPrice = newTriggerPrice;
        this.reason = reason;
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }
}
