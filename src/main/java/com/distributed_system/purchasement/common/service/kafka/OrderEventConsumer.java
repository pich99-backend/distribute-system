package com.distributed_system.purchasement.common.service.kafka;

import com.distributed_system.purchasement.common.constant.KafkaTopics;
import com.distributed_system.purchasement.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Order Event Consumer with:
 * - Single message processing
 * - Batch processing for high throughput
 * - Multiple topics in one listener
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final KafkaProducerService kafkaProducerService;

    /**
     * Listen for order created events
     */
    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = KafkaTopics.GROUP_ORDER_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("Received ORDER_CREATED event: orderId={}, userId={}, total={}",
                event.getOrderId(), event.getUserId(), event.getTotalAmount());

        try {
            processOrderCreated(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed ORDER_CREATED event: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing ORDER_CREATED event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Listen for order completed events
     */
    @KafkaListener(
            topics = KafkaTopics.ORDER_COMPLETED,
            groupId = KafkaTopics.GROUP_ORDER_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCompleted(
            @Payload OrderEvent event,
            Acknowledgment acknowledgment) {

        log.info("Received ORDER_COMPLETED event: orderId={}", event.getOrderId());

        try {
            processOrderCompleted(event);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing ORDER_COMPLETED event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Listen for order cancelled events
     */
    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            groupId = KafkaTopics.GROUP_ORDER_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(
            @Payload OrderEvent event,
            Acknowledgment acknowledgment) {

        log.info("Received ORDER_CANCELLED event: orderId={}, userId={}",
                event.getOrderId(), event.getUserId());

        try {
            processOrderCancelled(event);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing ORDER_CANCELLED event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Batch listener for high-throughput processing
     * Processes multiple messages at once
     */
    @KafkaListener(
            topics = KafkaTopics.ORDER_UPDATED,
            groupId = KafkaTopics.GROUP_ORDER_SERVICE,
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void handleOrderUpdatedBatch(
            @Payload List<OrderEvent> events,
            Acknowledgment acknowledgment) {

        log.info("Received batch of {} ORDER_UPDATED events", events.size());

        try {
            for (OrderEvent event : events) {
                processOrderUpdated(event);
            }

            // Acknowledge all messages in the batch
            acknowledgment.acknowledge();
            log.info("Successfully processed batch of {} ORDER_UPDATED events", events.size());

        } catch (Exception e) {
            log.error("Error processing ORDER_UPDATED batch: {}", e.getMessage());
            throw e;
        }
    }

    // ==================== Business Logic ====================

    private void processOrderCreated(OrderEvent event) {
        log.debug("Processing order creation: orderId={}, items={}",
                event.getOrderId(), event.getItems() != null ? event.getItems().size() : 0);

        // Business logic examples:
        // 1. Validate order
        // 2. Reserve inventory
        // 3. Calculate shipping
        // 4. Send notification to user

        // Example: Trigger notification
        // notificationService.sendOrderConfirmation(event.getUserId(), event.getOrderId());
    }

    private void processOrderUpdated(OrderEvent event) {
        log.debug("Processing order update: orderId={}, status={}",
                event.getOrderId(), event.getStatus());

        // Update order status in database
        // Notify relevant services
    }

    private void processOrderCompleted(OrderEvent event) {
        log.debug("Processing order completion: orderId={}", event.getOrderId());

        // Business logic:
        // 1. Update order status
        // 2. Release reserved inventory
        // 3. Update analytics
        // 4. Send completion notification
    }

    private void processOrderCancelled(OrderEvent event) {
        log.debug("Processing order cancellation: orderId={}", event.getOrderId());

        // Business logic:
        // 1. Update order status to cancelled
        // 2. Restore inventory
        // 3. Process refund if payment was made
        // 4. Send cancellation notification
    }
}

