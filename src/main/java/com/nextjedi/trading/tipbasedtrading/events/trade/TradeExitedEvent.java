package com.nextjedi.trading.tipbasedtrading.events.trade;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when a trade exit order is executed
 */
@Getter
public class TradeExitedEvent extends DomainEvent {
    private final String tradeId;
    private final String orderId;
    private final String portfolioId;
    private final String strategyId;
    private final Long instrumentToken;
    private final Double exitPrice;
    private final Integer quantity;
    private final Double pnl;
    private final String exitReason; // STOP_LOSS, TARGET, MANUAL

    @Builder
    public TradeExitedEvent(String tradeId, String orderId, String portfolioId,
                           String strategyId, Long instrumentToken,
                           Double exitPrice, Integer quantity, Double pnl,
                           String exitReason) {
        super();
        this.tradeId = tradeId;
        this.orderId = orderId;
        this.portfolioId = portfolioId;
        this.strategyId = strategyId;
        this.instrumentToken = instrumentToken;
        this.exitPrice = exitPrice;
        this.quantity = quantity;
        this.pnl = pnl;
        this.exitReason = exitReason;
    }

    @Override
    public String getAggregateId() {
        return tradeId;
    }
}
