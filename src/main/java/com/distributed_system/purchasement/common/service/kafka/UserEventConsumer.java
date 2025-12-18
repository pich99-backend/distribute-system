package com.distributed_system.purchasement.common.service.kafka;

import com.distributed_system.purchasement.common.constant.KafkaTopics;
import com.distributed_system.purchasement.common.event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * User Event Consumer with:
 * - Manual acknowledgment
 * - Error handling
 * - Header access
 * - Different listeners for different event types
 */
@Slf4j
@Service
public class UserEventConsumer {

    /**
     * Listen for user created events
     * Manual acknowledgment for reliability
     */
    @KafkaListener(
            topics = KafkaTopics.USER_CREATED,
            groupId = KafkaTopics.GROUP_USER_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserCreated(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received USER_CREATED event: eventId={}, topic={}, partition={}, offset={}",
                event.getEventId(), topic, partition, offset);

        try {
            // Process the event
            processUserCreated(event);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed USER_CREATED event: {}", event.getUsername());

        } catch (Exception e) {
            log.error("Error processing USER_CREATED event: {}", e.getMessage());
            // Don't acknowledge - message will be redelivered or sent to DLQ
            throw e;
        }
    }

    /**
     * Listen for user updated events
     */
    @KafkaListener(
            topics = KafkaTopics.USER_UPDATED,
            groupId = KafkaTopics.GROUP_USER_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserUpdated(
            @Payload UserEvent event,
            Acknowledgment acknowledgment) {

        log.info("Received USER_UPDATED event: eventId={}, username={}",
                event.getEventId(), event.getUsername());

        try {
            processUserUpdated(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed USER_UPDATED event");

        } catch (Exception e) {
            log.error("Error processing USER_UPDATED event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Listen for user deleted events
     */
    @KafkaListener(
            topics = KafkaTopics.USER_DELETED,
            groupId = KafkaTopics.GROUP_USER_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserDeleted(
            @Payload UserEvent event,
            Acknowledgment acknowledgment) {

        log.info("Received USER_DELETED event: eventId={}, userId={}",
                event.getEventId(), event.getUserId());

        try {
            processUserDeleted(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed USER_DELETED event");

        } catch (Exception e) {
            log.error("Error processing USER_DELETED event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Alternative: Listen using ConsumerRecord for full access to metadata
     */
    // @KafkaListener(topics = "user-topic-raw", groupId = "user-raw-group")
    public void handleRawRecord(ConsumerRecord<String, UserEvent> record, Acknowledgment ack) {
        log.info("Received raw record: key={}, value={}, topic={}, partition={}, offset={}, timestamp={}",
                record.key(),
                record.value(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp());

        // Access headers
        record.headers().forEach(header ->
                log.info("Header: {}={}", header.key(), new String(header.value())));

        ack.acknowledge();
    }

    // ==================== Business Logic ====================

    private void processUserCreated(UserEvent event) {
        // Implement your business logic here
        log.debug("Processing user creation: username={}, email={}, age={}",
                event.getUsername(), event.getEmail(), event.getAge());

        // Example: Save to database, send welcome email, etc.
        // userRepository.save(new User(event.getUsername(), event.getEmail(), event.getAge()));
        // emailService.sendWelcomeEmail(event.getEmail());

        // Simulate processing
        simulateProcessing();
    }

    private void processUserUpdated(UserEvent event) {
        log.debug("Processing user update: userId={}, username={}",
                event.getUserId(), event.getUsername());

        // Example: Update database record
        // userRepository.updateUser(event.getUserId(), event.getUsername(), event.getEmail());

        simulateProcessing();
    }

    private void processUserDeleted(UserEvent event) {
        log.debug("Processing user deletion: userId={}", event.getUserId());

        // Example: Soft delete or remove user
        // userRepository.deleteById(event.getUserId());

        simulateProcessing();
    }

    private void simulateProcessing() {
        try {
            // Simulate some processing time
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

