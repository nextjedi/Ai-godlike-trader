package com.nextjedi.trading.tipbasedtrading.events.trade;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

/**
 * Event published when a trade is fully completed (both entry and exit executed)
 */
@Getter
public class TradeCompletedEvent extends DomainEvent {
    private final String tradeId;
    private final String portfolioId;
    private final String strategyId;
    private final Long instrumentToken;
    private final String tradingSymbol;
    private final Double entryPrice;
    private final Double exitPrice;
    private final Integer quantity;
    private final Double pnl;
    private final Double pnlPercentage;
    private final Instant entryTime;
    private final Instant exitTime;
    private final Duration tradeDuration;
    private final String exitReason;

    @Builder
    public TradeCompletedEvent(String tradeId, String portfolioId, String strategyId,
                              Long instrumentToken, String tradingSymbol,
                              Double entryPrice, Double exitPrice, Integer quantity,
                              Double pnl, Instant entryTime, Instant exitTime,
                              String exitReason) {
        super();
        this.tradeId = tradeId;
        this.portfolioId = portfolioId;
        this.strategyId = strategyId;
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.entryPrice = entryPrice;
        this.exitPrice = exitPrice;
        this.quantity = quantity;
        this.pnl = pnl;
        this.pnlPercentage = ((exitPrice - entryPrice) / entryPrice) * 100;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.tradeDuration = Duration.between(entryTime, exitTime);
        this.exitReason = exitReason;
    }

    @Override
    public String getAggregateId() {
        return tradeId;
    }
}
