package com.nextjedi.trading.tipbasedtrading.events.tick;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Event published when a market tick is received from the broker
 */
@Getter
public class TickReceivedEvent extends DomainEvent {
    private final Long instrumentToken;
    private final String tradingSymbol;
    private final Double lastPrice;
    private final Integer volume;
    private final Double bidPrice;
    private final Double askPrice;
    private final Integer openInterest;
    private final Instant tickTimestamp;

    @Builder
    public TickReceivedEvent(Long instrumentToken, String tradingSymbol,
                            Double lastPrice, Integer volume,
                            Double bidPrice, Double askPrice,
                            Integer openInterest, Instant tickTimestamp) {
        super();
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.lastPrice = lastPrice;
        this.volume = volume;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.openInterest = openInterest;
        this.tickTimestamp = tickTimestamp != null ? tickTimestamp : Instant.now();
    }

    @Override
    public String getAggregateId() {
        return instrumentToken.toString();
    }
}
