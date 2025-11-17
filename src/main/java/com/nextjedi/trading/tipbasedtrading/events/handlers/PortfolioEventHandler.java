package com.nextjedi.trading.tipbasedtrading.events.handlers;

import com.nextjedi.trading.tipbasedtrading.events.portfolio.FundsAllocatedEvent;
import com.nextjedi.trading.tipbasedtrading.events.portfolio.PositionClosedEvent;
import com.nextjedi.trading.tipbasedtrading.events.portfolio.PositionOpenedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles portfolio-related domain events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioEventHandler {

    /**
     * Handle funds allocated event
     */
    @Async
    @EventListener
    public void handleFundsAllocated(FundsAllocatedEvent event) {
        log.info("Funds allocated - Portfolio: {}, Strategy: {}, Amount: ₹{}, Type: {}, Max Position: ₹{}",
                event.getPortfolioId(),
                event.getStrategyName(),
                event.getAllocatedAmount(),
                event.getAllocationType(),
                event.getMaxPositionSize());

        // TODO: Update strategy available capital
        // strategyService.updateAvailableCapital(event.getStrategyId(), event.getAllocatedAmount());

        // TODO: Recalculate position limits
        // riskService.updatePositionLimits(event);
    }

    /**
     * Handle position opened event
     */
    @Async
    @EventListener
    public void handlePositionOpened(PositionOpenedEvent event) {
        log.info("Position opened - Portfolio: {}, Strategy: {}, Symbol: {}, Qty: {}, Avg Price: {}, Total: ₹{}",
                event.getPortfolioId(),
                event.getStrategyId(),
                event.getTradingSymbol(),
                event.getQuantity(),
                event.getAveragePrice(),
                event.getTotalValue());

        // TODO: Update portfolio exposure
        // portfolioService.updateExposure(event.getPortfolioId(), event.getTotalValue());

        // TODO: Check risk limits
        // riskService.checkRiskLimits(event.getPortfolioId(), event.getStrategyId());
    }

    /**
     * Handle position closed event
     */
    @Async
    @EventListener
    public void handlePositionClosed(PositionClosedEvent event) {
        log.info("Position closed - Portfolio: {}, Strategy: {}, Symbol: {}, Entry: {}, Exit: {}, P&L: ₹{} ({:.2f}%)",
                event.getPortfolioId(),
                event.getStrategyId(),
                event.getTradingSymbol(),
                event.getAverageEntryPrice(),
                event.getAverageExitPrice(),
                event.getPnl(),
                event.getPnlPercentage());

        // TODO: Update portfolio P&L
        // portfolioService.updatePnL(event.getPortfolioId(), event.getPnl());

        // TODO: Update strategy P&L
        // strategyService.updatePnL(event.getStrategyId(), event.getPnl());

        // TODO: Release capital
        // portfolioService.releaseCapital(event.getPortfolioId(), event.getStrategyId(),
        //                                 event.getQuantity() * event.getAverageEntryPrice());
    }
}
