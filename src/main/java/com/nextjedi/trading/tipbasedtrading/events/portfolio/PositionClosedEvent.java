package com.nextjedi.trading.tipbasedtrading.events.portfolio;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when a position is closed in the portfolio
 */
@Getter
public class PositionClosedEvent extends DomainEvent {
    private final String portfolioId;
    private final String strategyId;
    private final String tradeId;
    private final Long instrumentToken;
    private final String tradingSymbol;
    private final Integer quantity;
    private final Double averageEntryPrice;
    private final Double averageExitPrice;
    private final Double pnl;
    private final Double pnlPercentage;

    @Builder
    public PositionClosedEvent(String portfolioId, String strategyId, String tradeId,
                              Long instrumentToken, String tradingSymbol,
                              Integer quantity, Double averageEntryPrice,
                              Double averageExitPrice, Double pnl, Double pnlPercentage) {
        super();
        this.portfolioId = portfolioId;
        this.strategyId = strategyId;
        this.tradeId = tradeId;
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.quantity = quantity;
        this.averageEntryPrice = averageEntryPrice;
        this.averageExitPrice = averageExitPrice;
        this.pnl = pnl;
        this.pnlPercentage = pnlPercentage;
    }

    @Override
    public String getAggregateId() {
        return portfolioId;
    }
}
