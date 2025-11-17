package com.nextjedi.trading.tipbasedtrading.events.trade;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when a new trade is created
 */
@Getter
public class TradeCreatedEvent extends DomainEvent {
    private final String tradeId;
    private final String portfolioId;
    private final String strategyId;
    private final Long instrumentToken;
    private final String tradingSymbol;
    private final Double triggerPrice;
    private final Double stopLoss;
    private final Double target;
    private final String tradeType;

    @Builder
    public TradeCreatedEvent(String tradeId, String portfolioId, String strategyId,
                            Long instrumentToken, String tradingSymbol,
                            Double triggerPrice, Double stopLoss, Double target,
                            String tradeType) {
        super();
        this.tradeId = tradeId;
        this.portfolioId = portfolioId;
        this.strategyId = strategyId;
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.triggerPrice = triggerPrice;
        this.stopLoss = stopLoss;
        this.target = target;
        this.tradeType = tradeType;
    }

    @Override
    public String getAggregateId() {
        return tradeId;
    }
}
