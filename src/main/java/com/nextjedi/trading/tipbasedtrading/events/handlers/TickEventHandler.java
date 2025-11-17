package com.nextjedi.trading.tipbasedtrading.events.handlers;

import com.nextjedi.trading.tipbasedtrading.events.tick.TickReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles tick-related domain events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TickEventHandler {

    /**
     * Handle tick received event
     * - Route tick to relevant strategies
     * - Store tick data (if enabled)
     * - Update market data cache
     */
    @Async
    @EventListener
    public void handleTickReceived(TickReceivedEvent event) {
        log.trace("Tick received - Token: {}, Symbol: {}, LTP: {}, Volume: {}, OI: {}",
                event.getInstrumentToken(),
                event.getTradingSymbol(),
                event.getLastPrice(),
                event.getVolume(),
                event.getOpenInterest());

        // TODO: Route tick to active trades monitoring this instrument
        // tradeExecutionService.processTick(event);

        // TODO: Store tick data in Redis/Kafka for analytics
        // tickDataService.storeTick(event);

        // TODO: Update market data cache
        // marketDataCache.updateLastPrice(event.getInstrumentToken(), event.getLastPrice());

        // TODO: Route to strategies interested in this instrument
        // strategyRouter.routeTickToStrategies(event);
    }
}
