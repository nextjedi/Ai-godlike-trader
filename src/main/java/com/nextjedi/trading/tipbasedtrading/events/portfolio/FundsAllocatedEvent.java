package com.nextjedi.trading.tipbasedtrading.events.portfolio;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * Event published when funds are allocated to a strategy
 */
@Getter
public class FundsAllocatedEvent extends DomainEvent {
    private final String portfolioId;
    private final String strategyId;
    private final String strategyName;
    private final Double allocatedAmount;
    private final String allocationType; // PERCENTAGE, FIXED_AMOUNT
    private final Double maxPositionSize;

    @Builder
    public FundsAllocatedEvent(String portfolioId, String strategyId, String strategyName,
                              Double allocatedAmount, String allocationType,
                              Double maxPositionSize) {
        super();
        this.portfolioId = portfolioId;
        this.strategyId = strategyId;
        this.strategyName = strategyName;
        this.allocatedAmount = allocatedAmount;
        this.allocationType = allocationType;
        this.maxPositionSize = maxPositionSize;
    }

    @Override
    public String getAggregateId() {
        return portfolioId;
    }
}
