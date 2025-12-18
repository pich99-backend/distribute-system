package com.distributed_system.purchasement.common.service.kafka;

import com.distributed_system.purchasement.common.constant.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Dead Letter Queue Consumer
 * Handles messages that failed processing after retries
 * 
 * Options for handling DLQ messages:
 * 1. Log and alert for manual investigation
 * 2. Store in database for later retry
 * 3. Transform and republish to original topic
 * 4. Send to external monitoring system
 */
@Slf4j
@Service
public class DeadLetterQueueConsumer {

    /**
     * Handle failed user events
     * Uses special factory without DLQ to avoid infinite loops
     */
    @KafkaListener(
            topics = KafkaTopics.DLQ_USER,
            groupId = KafkaTopics.GROUP_DLQ_HANDLER,
            containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void handleUserDLQ(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {
        handleDeadLetter("USER", record, acknowledgment);
    }

    /**
     * Handle failed order events
     */
    @KafkaListener(
            topics = KafkaTopics.DLQ_ORDER,
            groupId = KafkaTopics.GROUP_DLQ_HANDLER,
            containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void handleOrderDLQ(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {
        handleDeadLetter("ORDER", record, acknowledgment);
    }

    /**
     * Handle failed payment events
     */
    @KafkaListener(
            topics = KafkaTopics.DLQ_PAYMENT,
            groupId = KafkaTopics.GROUP_DLQ_HANDLER,
            containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void handlePaymentDLQ(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {
        handleDeadLetter("PAYMENT", record, acknowledgment);
    }

    /**
     * Generic DLQ handler using pattern matching
     * Catches any DLQ topic starting with "dlq-"
     */
    @KafkaListener(
            topicPattern = "dlq-.*",
            groupId = "dlq-generic-handler",
            containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void handleGenericDLQ(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {
        handleDeadLetter("GENERIC", record, acknowledgment);
    }

    // ==================== Common DLQ Handler ====================

    private void handleDeadLetter(String type, ConsumerRecord<String, Object> record,
                                  Acknowledgment acknowledgment) {
        log.error("=== DEAD LETTER RECEIVED [{} DLQ] ===", type);
        log.error("Topic: {}", record.topic());
        log.error("Partition: {}", record.partition());
        log.error("Offset: {}", record.offset());
        log.error("Key: {}", record.key());
        log.error("Timestamp: {}", record.timestamp());

        // Log headers (contains error information)
        record.headers().forEach(header -> {
            String value = new String(header.value(), StandardCharsets.UTF_8);
            log.error("Header [{}]: {}", header.key(), value);
        });

        // Log message content
        log.error("Value: {}", record.value());

        try {
            // ===== Option 1: Just log and acknowledge (for analysis) =====
            logForInvestigation(type, record);

            // ===== Option 2: Store in database for manual retry =====
            // storeForManualRetry(type, record);

            // ===== Option 3: Send alert to monitoring system =====
            // sendAlert(type, record);

            // ===== Option 4: Attempt to fix and republish =====
            // attemptRecovery(type, record);

            // Acknowledge after handling
            acknowledgment.acknowledge();
            log.info("DLQ message acknowledged: topic={}, offset={}", record.topic(), record.offset());

        } catch (Exception e) {
            log.error("Error handling DLQ message: {}", e.getMessage());
            // Still acknowledge to prevent infinite loop
            acknowledgment.acknowledge();
        }
    }

    // ==================== DLQ Handling Strategies ====================

    /**
     * Strategy 1: Log for manual investigation
     */
    private void logForInvestigation(String type, ConsumerRecord<String, Object> record) {
        log.warn("DLQ message logged for investigation: type={}, topic={}, key={}, offset={}",
                type, record.topic(), record.key(), record.offset());

        // In production, you might:
        // - Write to a special log file
        // - Send to ELK stack or similar
        // - Create a ticket in issue tracking system
    }

    /**
     * Strategy 2: Store in database for manual retry
     */
    private void storeForManualRetry(String type, ConsumerRecord<String, Object> record) {
        // Example: Save to a dead_letter table
        /*
        DeadLetterEntity entity = new DeadLetterEntity();
        entity.setType(type);
        entity.setOriginalTopic(extractOriginalTopic(record.topic()));
        entity.setKey(record.key());
        entity.setValue(record.value().toString());
        entity.setErrorMessage(extractErrorMessage(record));
        entity.setTimestamp(LocalDateTime.now());
        entity.setStatus("PENDING");
        deadLetterRepository.save(entity);
        */

        log.info("DLQ message stored in database for retry");
    }

    /**
     * Strategy 3: Send alert to monitoring system
     */
    private void sendAlert(String type, ConsumerRecord<String, Object> record) {
        // Example: Send to Slack, PagerDuty, email, etc.
        /*
        alertService.sendAlert(
            AlertLevel.ERROR,
            "Dead Letter Queue Alert",
            String.format("Failed message in %s DLQ: topic=%s, offset=%d",
                type, record.topic(), record.offset())
        );
        */

        log.info("Alert sent for DLQ message");
    }

    /**
     * Strategy 4: Attempt automatic recovery
     */
    private void attemptRecovery(String type, ConsumerRecord<String, Object> record) {
        // Example: Transform message and republish
        /*
        String originalTopic = record.topic().replace("dlq-", "");
        
        // Maybe fix the message format or add missing fields
        Object fixedMessage = attemptToFixMessage(record.value());
        
        if (fixedMessage != null) {
            kafkaTemplate.send(originalTopic, record.key(), fixedMessage);
            log.info("Message recovered and republished to {}", originalTopic);
        }
        */

        log.info("Recovery attempted for DLQ message");
    }

    /**
     * Extract error message from headers
     */
    private String extractErrorMessage(ConsumerRecord<String, Object> record) {
        var header = record.headers().lastHeader("kafka_dlt-exception-message");
        if (header != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return "Unknown error";
    }
}

