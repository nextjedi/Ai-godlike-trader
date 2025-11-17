package com.nextjedi.trading.tipbasedtrading.broker;

import com.nextjedi.trading.tipbasedtrading.broker.dto.Tick;

import java.util.List;

/**
 * Listener interface for receiving real-time tick data from broker
 */
public interface TickListener {

    /**
     * Called when ticks are received from the broker
     * @param ticks List of ticks received
     */
    void onTicksReceived(List<Tick> ticks);

    /**
     * Called when connection is established
     */
    void onConnected();

    /**
     * Called when connection is disconnected
     */
    void onDisconnected();

    /**
     * Called when an error occurs
     * @param exception The exception
     */
    void onError(Exception exception);

    /**
     * Called when tick subscription is successful
     */
    void onSubscribed();

    /**
     * Called when tick unsubscription is successful
     */
    void onUnsubscribed();
}
