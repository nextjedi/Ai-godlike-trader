package com.nextjedi.trading.tipbasedtrading.health;

import com.nextjedi.trading.tipbasedtrading.broker.TradingBroker;
import com.nextjedi.trading.tipbasedtrading.broker.factory.BrokerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BrokerHealthIndicator
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Broker Health Indicator Tests")
class BrokerHealthIndicatorTest {

    @Mock
    private BrokerFactory brokerFactory;

    @Mock
    private TradingBroker tradingBroker;

    private BrokerHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new BrokerHealthIndicator(brokerFactory);
    }

    @Test
    @DisplayName("Should return UP when broker is connected")
    void shouldReturnUpWhenBrokerIsConnected() {
        // Given
        when(brokerFactory.getDefaultBroker()).thenReturn(tradingBroker);
        when(tradingBroker.isConnected()).thenReturn(true);
        when(tradingBroker.getBrokerName()).thenReturn("Zerodha");
        when(tradingBroker.getUserId()).thenReturn("TEST123");

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("broker", "Zerodha");
        assertThat(health.getDetails()).containsEntry("status", "connected");
        assertThat(health.getDetails()).containsEntry("userId", "TEST123");
    }

    @Test
    @DisplayName("Should return DOWN when broker is disconnected")
    void shouldReturnDownWhenBrokerIsDisconnected() {
        // Given
        when(brokerFactory.getDefaultBroker()).thenReturn(tradingBroker);
        when(tradingBroker.isConnected()).thenReturn(false);
        when(tradingBroker.getBrokerName()).thenReturn("Zerodha");

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("broker", "Zerodha");
        assertThat(health.getDetails()).containsEntry("status", "disconnected");
    }

    @Test
    @DisplayName("Should handle broker factory exception")
    void shouldHandleBrokerFactoryException() {
        // Given
        when(brokerFactory.getDefaultBroker()).thenThrow(new RuntimeException("Broker initialization failed"));

        // When/Then
        assertThatThrownBy(() -> healthIndicator.health())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Broker initialization failed");
    }
}
