package com.nextjedi.trading.tipbasedtrading.broker;

import com.nextjedi.trading.tipbasedtrading.broker.dto.*;

import java.util.List;

/**
 * Abstract interface for trading brokers
 * Implements Strategy Pattern to support multiple trading platforms
 */
public interface TradingBroker {

    /**
     * Get the broker name (e.g., "zerodha", "upstox", "interactive-brokers")
     */
    String getBrokerName();

    /**
     * Connect to the broker and authenticate
     */
    void connect();

    /**
     * Disconnect from the broker
     */
    void disconnect();

    /**
     * Check if connected to the broker
     */
    boolean isConnected();

    /**
     * Place a new order
     * @param request Order request details
     * @return Order response with order ID
     */
    OrderResponse placeOrder(OrderRequest request);

    /**
     * Modify an existing order
     * @param orderId The order ID to modify
     * @param request Modified order details
     * @return Order response
     */
    OrderResponse modifyOrder(String orderId, OrderRequest request);

    /**
     * Cancel an existing order
     * @param orderId The order ID to cancel
     * @return Order response
     */
    OrderResponse cancelOrder(String orderId);

    /**
     * Get order details
     * @param orderId The order ID
     * @return Order details
     */
    OrderDetail getOrder(String orderId);

    /**
     * Get all orders for the day
     * @return List of orders
     */
    List<OrderDetail> getOrders();

    /**
     * Get current positions
     * @return List of positions
     */
    List<Position> getPositions();

    /**
     * Get account balance and margin
     * @return Balance details
     */
    Balance getBalance();

    /**
     * Get instruments for a given exchange
     * @param exchange Exchange name (e.g., "NSE", "NFO")
     * @return List of instruments
     */
    List<Instrument> getInstruments(String exchange);

    /**
     * Get instrument by token
     * @param instrumentToken The instrument token
     * @return Instrument details
     */
    Instrument getInstrument(Long instrumentToken);

    /**
     * Subscribe to real-time market data ticks
     * @param instrumentTokens List of instrument tokens to subscribe
     * @param listener Listener to receive tick callbacks
     */
    void subscribeToTicks(List<Long> instrumentTokens, TickListener listener);

    /**
     * Unsubscribe from real-time market data ticks
     * @param instrumentTokens List of instrument tokens to unsubscribe
     */
    void unsubscribeFromTicks(List<Long> instrumentTokens);

    /**
     * Get the user ID for this broker connection
     */
    String getUserId();
}
