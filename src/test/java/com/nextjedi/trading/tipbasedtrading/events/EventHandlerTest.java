package com.nextjedi.trading.tipbasedtrading.events;

import com.nextjedi.trading.tipbasedtrading.events.handlers.OrderEventHandler;
import com.nextjedi.trading.tipbasedtrading.events.handlers.PortfolioEventHandler;
import com.nextjedi.trading.tipbasedtrading.events.handlers.TradeEventHandler;
import com.nextjedi.trading.tipbasedtrading.events.order.OrderPlacedEvent;
import com.nextjedi.trading.tipbasedtrading.events.order.OrderExecutedEvent;
import com.nextjedi.trading.tipbasedtrading.events.portfolio.FundsAllocatedEvent;
import com.nextjedi.trading.tipbasedtrading.events.portfolio.PositionOpenedEvent;
import com.nextjedi.trading.tipbasedtrading.events.trade.TradeCreatedEvent;
import com.nextjedi.trading.tipbasedtrading.events.trade.TradeCompletedEvent;
import com.nextjedi.trading.tipbasedtrading.models.StrategyAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Event Handlers
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Event Handler Tests")
class EventHandlerTest {

    @InjectMocks
    private TradeEventHandler tradeEventHandler;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @InjectMocks
    private PortfolioEventHandler portfolioEventHandler;

    @Test
    @DisplayName("Should handle TradeCreatedEvent")
    void shouldHandleTradeCreatedEvent() {
        // Given
        TradeCreatedEvent event = TradeCreatedEvent.builder()
                .tradeId(1L)
                .tradingSymbol("NIFTY25JAN45000CE")
                .instrumentToken(12345L)
                .tradeType("BUY")
                .triggerPrice(100.0)
                .quantity(50)
                .portfolioId(1L)
                .strategyId("strategy1")
                .build();

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> tradeEventHandler.handleTradeCreated(event));
    }

    @Test
    @DisplayName("Should handle TradeCompletedEvent")
    void shouldHandleTradeCompletedEvent() {
        // Given
        TradeCompletedEvent event = TradeCompletedEvent.builder()
                .tradeId(1L)
                .tradingSymbol("NIFTY25JAN45000CE")
                .instrumentToken(12345L)
                .entryPrice(100.0)
                .exitPrice(120.0)
                .pnl(1000.0)
                .pnlPercentage(20.0)
                .tradeDuration(Duration.ofMinutes(30))
                .portfolioId(1L)
                .strategyId("strategy1")
                .build();

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> tradeEventHandler.handleTradeCompleted(event));
    }

    @Test
    @DisplayName("Should handle OrderPlacedEvent")
    void shouldHandleOrderPlacedEvent() {
        // Given
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId("order123")
                .tradeId(1L)
                .tradingSymbol("NIFTY25JAN45000CE")
                .orderType("LIMIT")
                .transactionType("BUY")
                .quantity(50)
                .price(100.0)
                .build();

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> orderEventHandler.handleOrderPlaced(event));
    }

    @Test
    @DisplayName("Should handle OrderExecutedEvent")
    void shouldHandleOrderExecutedEvent() {
        // Given
        OrderExecutedEvent event = OrderExecutedEvent.builder()
                .orderId("order123")
                .tradeId(1L)
                .tradingSymbol("NIFTY25JAN45000CE")
                .transactionType("BUY")
                .quantity(50)
                .averagePrice(99.5)
                .totalValue(4975.0)
                .build();

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> orderEventHandler.handleOrderExecuted(event));
    }

    @Test
    @DisplayName("Should handle FundsAllocatedEvent")
    void shouldHandleFundsAllocatedEvent() {
        // Given
        FundsAllocatedEvent event = FundsAllocatedEvent.builder()
                .portfolioId(1L)
                .strategyId("strategy1")
                .strategyName("Test Strategy")
                .allocationType(StrategyAllocation.AllocationType.PERCENTAGE)
                .allocatedAmount(20000.0)
                .maxPositionSize(5000.0)
                .build();

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> portfolioEventHandler.handleFundsAllocated(event));
    }

    @Test
    @DisplayName("Should handle PositionOpenedEvent")
    void shouldHandlePositionOpenedEvent() {
        // Given
        PositionOpenedEvent event = PositionOpenedEvent.builder()
                .portfolioId(1L)
                .strategyId("strategy1")
                .tradingSymbol("NIFTY25JAN45000CE")
                .quantity(50)
                .averagePrice(100.0)
                .totalValue(5000.0)
                .build();

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> portfolioEventHandler.handlePositionOpened(event));
    }

    @Test
    @DisplayName("TradeCreatedEvent should have valid event ID and timestamp")
    void tradeCreatedEventShouldHaveValidEventIdAndTimestamp() {
        // Given/When
        TradeCreatedEvent event = TradeCreatedEvent.builder()
                .tradeId(1L)
                .tradingSymbol("NIFTY25JAN45000CE")
                .instrumentToken(12345L)
                .tradeType("BUY")
                .triggerPrice(100.0)
                .quantity(50)
                .portfolioId(1L)
                .strategyId("strategy1")
                .build();

        // Then
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
        assertTrue(event.getOccurredOn().isBefore(Instant.now().plusSeconds(1)));
        assertTrue(event.getOccurredOn().isAfter(Instant.now().minusSeconds(10)));
    }

    @Test
    @DisplayName("OrderPlacedEvent should have unique event IDs")
    void orderPlacedEventShouldHaveUniqueEventIds() {
        // Given/When
        OrderPlacedEvent event1 = OrderPlacedEvent.builder()
                .orderId("order1")
                .tradeId(1L)
                .tradingSymbol("SYMBOL1")
                .orderType("LIMIT")
                .transactionType("BUY")
                .quantity(50)
                .price(100.0)
                .build();

        OrderPlacedEvent event2 = OrderPlacedEvent.builder()
                .orderId("order2")
                .tradeId(2L)
                .tradingSymbol("SYMBOL2")
                .orderType("LIMIT")
                .transactionType("BUY")
                .quantity(50)
                .price(100.0)
                .build();

        // Then
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }
}
