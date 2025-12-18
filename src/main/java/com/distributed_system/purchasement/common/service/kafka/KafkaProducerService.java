package com.distributed_system.purchasement.common.service.kafka;

import com.distributed_system.purchasement.common.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka Producer Service with:
 * - Async sending with callbacks
 * - Sync sending with timeout
 * - Custom headers support
 * - Error handling and logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ==================== Async Send Methods ====================

    /**
     * Send message asynchronously without waiting for result
     */
    public void sendAsync(String topic, Object message) {
        sendAsync(topic, null, message);
    }

    /**
     * Send message asynchronously with key
     */
    public void sendAsync(String topic, String key, Object message) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to send message to topic {}: {}", topic, exception.getMessage());
            } else {
                log.info("Message sent to topic {} partition {} offset {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * Send event asynchronously (uses eventId as key for ordering)
     */
    public void sendEventAsync(String topic, BaseEvent event) {
        String key = event.getEventId();
        sendAsyncWithHeaders(topic, key, event, event.getEventType(), event.getCorrelationId());
    }

    /**
     * Send message with custom headers
     */
    public void sendAsyncWithHeaders(String topic, String key, Object message,
                                     String eventType, String correlationId) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, message);

        // Add custom headers
        if (eventType != null) {
            record.headers().add(new RecordHeader("eventType",
                    eventType.getBytes(StandardCharsets.UTF_8)));
        }
        if (correlationId != null) {
            record.headers().add(new RecordHeader("correlationId",
                    correlationId.getBytes(StandardCharsets.UTF_8)));
        }
        record.headers().add(new RecordHeader("timestamp",
                String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)));

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to send message with headers to topic {}: {}",
                        topic, exception.getMessage());
            } else {
                log.info("Message with headers sent to topic {} partition {} offset {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    // ==================== Sync Send Methods ====================

    /**
     * Send message synchronously and wait for result
     * Returns true if successful, false otherwise
     */
    public boolean sendSync(String topic, Object message) {
        return sendSync(topic, null, message, 10);
    }

    /**
     * Send message synchronously with key
     */
    public boolean sendSync(String topic, String key, Object message) {
        return sendSync(topic, key, message, 10);
    }

    /**
     * Send message synchronously with custom timeout (in seconds)
     */
    public boolean sendSync(String topic, String key, Object message, int timeoutSeconds) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, message)
                    .get(timeoutSeconds, TimeUnit.SECONDS);

            log.info("Message sent synchronously to topic {} partition {} offset {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending message to topic {}: {}", topic, e.getMessage());
            return false;
        } catch (ExecutionException e) {
            log.error("Execution error sending message to topic {}: {}", topic, e.getCause().getMessage());
            return false;
        } catch (TimeoutException e) {
            log.error("Timeout sending message to topic {} after {} seconds", topic, timeoutSeconds);
            return false;
        }
    }

    /**
     * Send event synchronously
     */
    public boolean sendEventSync(String topic, BaseEvent event) {
        return sendSync(topic, event.getEventId(), event);
    }

    // ==================== Send to Partition ====================

    /**
     * Send message to specific partition
     */
    public void sendToPartition(String topic, int partition, String key, Object message) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, partition, key, message);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to send message to partition {} of topic {}: {}",
                        partition, topic, exception.getMessage());
            } else {
                log.info("Message sent to partition {} of topic {} at offset {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    // ==================== Callback-based Send ====================

    /**
     * Send message with custom success and error callbacks
     */
    public void sendWithCallback(String topic, String key, Object message,
                                 Runnable onSuccess,
                                 java.util.function.Consumer<Throwable> onError) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Message send failed: {}", exception.getMessage());
                if (onError != null) {
                    onError.accept(exception);
                }
            } else {
                log.info("Message sent successfully to {} at offset {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
        });
    }

    // ==================== Batch Send ====================

    /**
     * Send multiple messages to the same topic
     */
    public void sendBatch(String topic, java.util.List<?> messages) {
        messages.forEach(message -> sendAsync(topic, message));
        log.info("Batch of {} messages sent to topic {}", messages.size(), topic);
    }

    /**
     * Send multiple events with keys
     */
    public void sendEventBatch(String topic, java.util.List<? extends BaseEvent> events) {
        events.forEach(event -> sendEventAsync(topic, event));
        log.info("Batch of {} events sent to topic {}", events.size(), topic);
    }
}

