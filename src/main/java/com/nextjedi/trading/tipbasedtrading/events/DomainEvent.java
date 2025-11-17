package com.nextjedi.trading.tipbasedtrading.events;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events
 */
@Getter
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredOn;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }

    /**
     * Get the aggregate ID associated with this event
     */
    public abstract String getAggregateId();
}
