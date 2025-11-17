package com.nextjedi.trading.tipbasedtrading.events.trade;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when a trade entry order is executed
 */
@Getter
public class TradeEnteredEvent extends DomainEvent {
    private final String tradeId;
    private final String orderId;
    private final String portfolioId;
    private final String strategyId;
    private final Long instrumentToken;
    private final Double entryPrice;
    private final Integer quantity;
    private final Double totalValue;

    @Builder
    public TradeEnteredEvent(String tradeId, String orderId, String portfolioId,
                            String strategyId, Long instrumentToken,
                            Double entryPrice, Integer quantity, Double totalValue) {
        super();
        this.tradeId = tradeId;
        this.orderId = orderId;
        this.portfolioId = portfolioId;
        this.strategyId = strategyId;
        this.instrumentToken = instrumentToken;
        this.entryPrice = entryPrice;
        this.quantity = quantity;
        this.totalValue = totalValue;
    }

    @Override
    public String getAggregateId() {
        return tradeId;
    }
}
