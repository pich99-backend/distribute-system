package com.distributed_system.purchasement.common.service.kafka;

import com.distributed_system.purchasement.common.constant.KafkaTopics;
import com.distributed_system.purchasement.common.event.OrderEvent;
import com.distributed_system.purchasement.common.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Payment Event Consumer
 * Handles payment events and triggers follow-up actions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final KafkaProducerService kafkaProducerService;

    /**
     * Listen for payment completed events
     * Triggers order completion
     */
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = KafkaTopics.GROUP_PAYMENT_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(
            @Payload PaymentEvent event,
            Acknowledgment acknowledgment) {

        log.info("Received PAYMENT_COMPLETED event: paymentId={}, orderId={}, transactionId={}",
                event.getPaymentId(), event.getOrderId(), event.getTransactionId());

        try {
            // Process payment completion
            processPaymentCompleted(event);

            // Trigger order completion event (event chaining)
            OrderEvent orderCompleted = OrderEvent.completed(
                    event.getOrderId(),
                    event.getUserId(),
                    event.getAmount()
            );
            orderCompleted.setCorrelationId(event.getCorrelationId()); // Maintain correlation

            kafkaProducerService.sendEventAsync(KafkaTopics.ORDER_COMPLETED, orderCompleted);
            log.info("Triggered ORDER_COMPLETED event for orderId={}", event.getOrderId());

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing PAYMENT_COMPLETED event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Listen for payment failed events
     */
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = KafkaTopics.GROUP_PAYMENT_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(
            @Payload PaymentEvent event,
            Acknowledgment acknowledgment) {

        log.warn("Received PAYMENT_FAILED event: paymentId={}, orderId={}, reason={}",
                event.getPaymentId(), event.getOrderId(), event.getFailureReason());

        try {
            processPaymentFailed(event);

            // Optionally cancel the order
            OrderEvent orderCancelled = OrderEvent.cancelled(
                    event.getOrderId(),
                    event.getUserId()
            );
            orderCancelled.setCorrelationId(event.getCorrelationId());

            kafkaProducerService.sendEventAsync(KafkaTopics.ORDER_CANCELLED, orderCancelled);
            log.info("Triggered ORDER_CANCELLED event due to payment failure for orderId={}",
                    event.getOrderId());

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing PAYMENT_FAILED event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Listen for payment initiated events
     */
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_INITIATED,
            groupId = KafkaTopics.GROUP_PAYMENT_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentInitiated(
            @Payload PaymentEvent event,
            Acknowledgment acknowledgment) {

        log.info("Received PAYMENT_INITIATED event: paymentId={}, amount={}, method={}",
                event.getPaymentId(), event.getAmount(), event.getPaymentMethod());

        try {
            processPaymentInitiated(event);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing PAYMENT_INITIATED event: {}", e.getMessage());
            throw e;
        }
    }

    // ==================== Business Logic ====================

    private void processPaymentCompleted(PaymentEvent event) {
        log.debug("Processing payment completion: paymentId={}", event.getPaymentId());

        // Business logic:
        // 1. Update payment status in database
        // 2. Record transaction details
        // 3. Update accounting/finance systems
        // 4. Send payment confirmation to user
    }

    private void processPaymentFailed(PaymentEvent event) {
        log.debug("Processing payment failure: paymentId={}, reason={}",
                event.getPaymentId(), event.getFailureReason());

        // Business logic:
        // 1. Update payment status
        // 2. Log failure reason for analysis
        // 3. Notify user about failure
        // 4. Suggest alternative payment methods
    }

    private void processPaymentInitiated(PaymentEvent event) {
        log.debug("Processing payment initiation: paymentId={}", event.getPaymentId());

        // Business logic:
        // 1. Record payment attempt
        // 2. Connect to payment gateway
        // 3. Process payment
        // 4. Emit COMPLETED or FAILED event based on result
    }
}

