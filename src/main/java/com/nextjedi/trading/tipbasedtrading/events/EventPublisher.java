package com.nextjedi.trading.tipbasedtrading.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Service responsible for publishing domain events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publish a domain event
     * @param event The event to publish
     */
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {} with ID: {} for aggregate: {}",
                event.getEventType(), event.getEventId(), event.getAggregateId());

        try {
            applicationEventPublisher.publishEvent(event);
            log.debug("Successfully published event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish event: {} - Error: {}", event.getEventType(), e.getMessage(), e);
            throw new EventPublicationException("Failed to publish event: " + event.getEventType(), e);
        }
    }

    /**
     * Exception thrown when event publication fails
     */
    public static class EventPublicationException extends RuntimeException {
        public EventPublicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
