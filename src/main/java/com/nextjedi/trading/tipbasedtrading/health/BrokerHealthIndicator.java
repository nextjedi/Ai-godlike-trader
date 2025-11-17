package com.nextjedi.trading.tipbasedtrading.health;

import com.nextjedi.trading.tipbasedtrading.broker.BrokerFactory;
import com.nextjedi.trading.tipbasedtrading.broker.TradingBroker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrokerHealthIndicator implements HealthIndicator {

    private final BrokerFactory brokerFactory;

    @Override
    public Health health() {
        try {
            if (brokerFactory.getAvailableBrokers().isEmpty()) {
                return Health.down()
                        .withDetail("reason", "No brokers registered")
                        .build();
            }

            TradingBroker broker = brokerFactory.getDefaultBroker();
            boolean connected = broker.isConnected();

            if (connected) {
                return Health.up()
                        .withDetail("broker", broker.getBrokerName())
                        .withDetail("status", "connected")
                        .withDetail("userId", broker.getUserId())
                        .build();
            } else {
                return Health.down()
                        .withDetail("broker", broker.getBrokerName())
                        .withDetail("status", "disconnected")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error checking broker health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
