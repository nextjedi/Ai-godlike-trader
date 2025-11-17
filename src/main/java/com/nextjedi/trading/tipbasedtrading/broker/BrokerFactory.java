package com.nextjedi.trading.tipbasedtrading.broker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating trading broker instances
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BrokerFactory {

    private final Map<String, TradingBroker> brokers = new HashMap<>();

    /**
     * Register a broker implementation
     * @param broker The broker to register
     */
    public void registerBroker(TradingBroker broker) {
        String brokerName = broker.getBrokerName().toLowerCase();
        brokers.put(brokerName, broker);
        log.info("Registered broker: {}", brokerName);
    }

    /**
     * Get a broker by name
     * @param brokerName The broker name (e.g., "zerodha", "upstox")
     * @return The broker instance
     * @throws IllegalArgumentException if broker not found
     */
    public TradingBroker getBroker(String brokerName) {
        String normalizedName = brokerName.toLowerCase();
        TradingBroker broker = brokers.get(normalizedName);

        if (broker == null) {
            throw new IllegalArgumentException("Broker not found: " + brokerName +
                    ". Available brokers: " + brokers.keySet());
        }

        return broker;
    }

    /**
     * Get the default broker (first registered)
     * @return The default broker
     */
    public TradingBroker getDefaultBroker() {
        if (brokers.isEmpty()) {
            throw new IllegalStateException("No brokers registered");
        }

        return brokers.values().iterator().next();
    }

    /**
     * Check if a broker is registered
     * @param brokerName The broker name
     * @return true if registered, false otherwise
     */
    public boolean hasBroker(String brokerName) {
        return brokers.containsKey(brokerName.toLowerCase());
    }

    /**
     * Get all registered broker names
     * @return Set of broker names
     */
    public java.util.Set<String> getAvailableBrokers() {
        return brokers.keySet();
    }
}
