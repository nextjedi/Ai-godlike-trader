package com.nextjedi.trading.tipbasedtrading.events.portfolio;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when a new position is opened in the portfolio
 */
@Getter
public class PositionOpenedEvent extends DomainEvent {
    private final String portfolioId;
    private final String strategyId;
    private final String tradeId;
    private final Long instrumentToken;
    private final String tradingSymbol;
    private final Integer quantity;
    private final Double averagePrice;
    private final Double totalValue;

    @Builder
    public PositionOpenedEvent(String portfolioId, String strategyId, String tradeId,
                              Long instrumentToken, String tradingSymbol,
                              Integer quantity, Double averagePrice, Double totalValue) {
        super();
        this.portfolioId = portfolioId;
        this.strategyId = strategyId;
        this.tradeId = tradeId;
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.totalValue = totalValue;
    }

    @Override
    public String getAggregateId() {
        return portfolioId;
    }
}
