package com.nextjedi.trading.tipbasedtrading.events.handlers;

import com.nextjedi.trading.tipbasedtrading.events.order.OrderCancelledEvent;
import com.nextjedi.trading.tipbasedtrading.events.order.OrderExecutedEvent;
import com.nextjedi.trading.tipbasedtrading.events.order.OrderModifiedEvent;
import com.nextjedi.trading.tipbasedtrading.events.order.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles order-related domain events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {

    /**
     * Handle order placed event
     */
    @Async
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Order placed - ID: {}, Trade: {}, Symbol: {}, Type: {}, {} {} @ {}",
                event.getOrderId(),
                event.getTradeId(),
                event.getTradingSymbol(),
                event.getOrderType(),
                event.getTransactionType(),
                event.getQuantity(),
                event.getPrice());

        // TODO: Implement order tracking
        // orderTrackingService.trackOrder(event);

        // TODO: Audit logging
        // auditService.logOrderPlacement(event);
    }

    /**
     * Handle order executed event
     */
    @Async
    @EventListener
    public void handleOrderExecuted(OrderExecutedEvent event) {
        log.info("Order executed - ID: {}, Trade: {}, Symbol: {}, {} {} @ {} (Total: ₹{})",
                event.getOrderId(),
                event.getTradeId(),
                event.getTradingSymbol(),
                event.getTransactionType(),
                event.getQuantity(),
                event.getAveragePrice(),
                event.getTotalValue());

        // TODO: Update trade status based on order type (entry/exit)
        // tradeService.handleOrderExecution(event);

        // TODO: Audit logging
        // auditService.logOrderExecution(event);
    }

    /**
     * Handle order modified event
     */
    @Async
    @EventListener
    public void handleOrderModified(OrderModifiedEvent event) {
        log.info("Order modified - ID: {}, Trade: {}, Reason: {}, Price: {} -> {}, Trigger: {} -> {}",
                event.getOrderId(),
                event.getTradeId(),
                event.getReason(),
                event.getOldPrice(),
                event.getNewPrice(),
                event.getOldTriggerPrice(),
                event.getNewTriggerPrice());

        // TODO: Audit logging
        // auditService.logOrderModification(event);
    }

    /**
     * Handle order cancelled event
     */
    @Async
    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Order cancelled - ID: {}, Trade: {}, Reason: {}",
                event.getOrderId(),
                event.getTradeId(),
                event.getReason());

        // TODO: Handle trade cancellation if entry order
        // tradeService.handleOrderCancellation(event);

        // TODO: Audit logging
        // auditService.logOrderCancellation(event);
    }
}
