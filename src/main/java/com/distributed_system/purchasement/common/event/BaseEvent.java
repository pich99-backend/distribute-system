package com.distributed_system.purchasement.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base event class for all Kafka events
 * Contains common metadata for event tracking and debugging
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    /**
     * Unique event identifier for tracking
     */
    private String eventId;

    /**
     * Event type (e.g., "USER_CREATED", "ORDER_UPDATED")
     */
    private String eventType;

    /**
     * Timestamp when event was created
     */
    private LocalDateTime timestamp;

    /**
     * Source service that produced the event
     */
    private String source;

    /**
     * Correlation ID for tracing across services
     */
    private String correlationId;

    /**
     * Event version for schema evolution
     */
    private int version;

    /**
     * Initialize common fields before sending
     */
    public void initializeMetadata(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.source = source;
        this.version = 1;
        if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
    }
}

