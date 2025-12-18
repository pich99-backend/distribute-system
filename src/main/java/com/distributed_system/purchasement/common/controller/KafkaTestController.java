package com.distributed_system.purchasement.common.controller;

import com.distributed_system.purchasement.common.constant.KafkaTopics;
import com.distributed_system.purchasement.common.event.OrderEvent;
import com.distributed_system.purchasement.common.event.PaymentEvent;
import com.distributed_system.purchasement.common.event.UserEvent;
import com.distributed_system.purchasement.common.service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka Test Controller
 * Endpoints for testing all Kafka features
 */
@Slf4j
@RestController
@RequestMapping("/kafka/test")
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaProducerService kafkaProducerService;

    // ==================== User Events ====================

    /**
     * Test: Create user event (async)
     * POST /kafka/test/user/create?username=john&email=john@example.com&age=25
     */
    @PostMapping("/user/create")
    public ResponseEntity<Map<String, Object>> createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam int age) {

        UserEvent event = UserEvent.created(
                System.currentTimeMillis(), // Simulated userId
                username,
                email,
                age
        );

        kafkaProducerService.sendEventAsync(KafkaTopics.USER_CREATED, event);

        return ResponseEntity.ok(buildResponse("User created event sent", event));
    }

    /**
     * Test: Update user event
     * POST /kafka/test/user/update?userId=123&username=john&email=john@example.com&age=26
     */
    @PostMapping("/user/update")
    public ResponseEntity<Map<String, Object>> updateUser(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam int age) {

        UserEvent event = UserEvent.updated(userId, username, email, age);
        kafkaProducerService.sendEventAsync(KafkaTopics.USER_UPDATED, event);

        return ResponseEntity.ok(buildResponse("User updated event sent", event));
    }

    /**
     * Test: Delete user event
     * POST /kafka/test/user/delete?userId=123
     */
    @PostMapping("/user/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestParam Long userId) {

        UserEvent event = UserEvent.deleted(userId);
        kafkaProducerService.sendEventAsync(KafkaTopics.USER_DELETED, event);

        return ResponseEntity.ok(buildResponse("User deleted event sent", event));
    }

    // ==================== Order Events ====================

    /**
     * Test: Create order event
     * POST /kafka/test/order/create?userId=123&totalAmount=99.99
     */
    @PostMapping("/order/create")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam Long userId,
            @RequestParam BigDecimal totalAmount) {

        List<OrderEvent.OrderItem> items = Arrays.asList(
                new OrderEvent.OrderItem(1L, "Product A", 2, new BigDecimal("29.99")),
                new OrderEvent.OrderItem(2L, "Product B", 1, new BigDecimal("39.99"))
        );

        OrderEvent event = OrderEvent.created(
                System.currentTimeMillis(), // Simulated orderId
                userId,
                items,
                totalAmount
        );

        kafkaProducerService.sendEventAsync(KafkaTopics.ORDER_CREATED, event);

        return ResponseEntity.ok(buildResponse("Order created event sent", event));
    }

    /**
     * Test: Cancel order event
     * POST /kafka/test/order/cancel?orderId=123&userId=456
     */
    @PostMapping("/order/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId) {

        OrderEvent event = OrderEvent.cancelled(orderId, userId);
        kafkaProducerService.sendEventAsync(KafkaTopics.ORDER_CANCELLED, event);

        return ResponseEntity.ok(buildResponse("Order cancelled event sent", event));
    }

    /**
     * Test: Complete order event
     * POST /kafka/test/order/complete?orderId=123&userId=456&totalAmount=99.99
     */
    @PostMapping("/order/complete")
    public ResponseEntity<Map<String, Object>> completeOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam BigDecimal totalAmount) {

        OrderEvent event = OrderEvent.completed(orderId, userId, totalAmount);
        kafkaProducerService.sendEventAsync(KafkaTopics.ORDER_COMPLETED, event);

        return ResponseEntity.ok(buildResponse("Order completed event sent", event));
    }

    // ==================== Payment Events ====================

    /**
     * Test: Initiate payment event
     * POST /kafka/test/payment/initiate?orderId=123&userId=456&amount=99.99&method=CREDIT_CARD
     */
    @PostMapping("/payment/initiate")
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "CREDIT_CARD") String method) {

        PaymentEvent event = PaymentEvent.initiated(
                System.currentTimeMillis(), // Simulated paymentId
                orderId,
                userId,
                amount,
                "USD",
                method
        );

        kafkaProducerService.sendEventAsync(KafkaTopics.PAYMENT_INITIATED, event);

        return ResponseEntity.ok(buildResponse("Payment initiated event sent", event));
    }

    /**
     * Test: Complete payment event
     * POST /kafka/test/payment/complete?paymentId=123&orderId=456&transactionId=TXN123
     */
    @PostMapping("/payment/complete")
    public ResponseEntity<Map<String, Object>> completePayment(
            @RequestParam Long paymentId,
            @RequestParam Long orderId,
            @RequestParam String transactionId) {

        PaymentEvent event = PaymentEvent.completed(paymentId, orderId, transactionId);
        kafkaProducerService.sendEventAsync(KafkaTopics.PAYMENT_COMPLETED, event);

        return ResponseEntity.ok(buildResponse("Payment completed event sent", event));
    }

    /**
     * Test: Fail payment event
     * POST /kafka/test/payment/fail?paymentId=123&orderId=456&reason=Insufficient%20funds
     */
    @PostMapping("/payment/fail")
    public ResponseEntity<Map<String, Object>> failPayment(
            @RequestParam Long paymentId,
            @RequestParam Long orderId,
            @RequestParam String reason) {

        PaymentEvent event = PaymentEvent.failed(paymentId, orderId, reason);
        kafkaProducerService.sendEventAsync(KafkaTopics.PAYMENT_FAILED, event);

        return ResponseEntity.ok(buildResponse("Payment failed event sent", event));
    }

    // ==================== Advanced Tests ====================

    /**
     * Test: Send message synchronously (waits for confirmation)
     * POST /kafka/test/sync?topic=user-created&message=Hello
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sendSync(
            @RequestParam String topic,
            @RequestParam String message) {

        boolean success = kafkaProducerService.sendSync(topic, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("topic", topic);
        response.put("message", message);

        return success ?
                ResponseEntity.ok(response) :
                ResponseEntity.internalServerError().body(response);
    }

    /**
     * Test: Send to specific partition
     * POST /kafka/test/partition?topic=user-created&partition=0&key=user1&message=Hello
     */
    @PostMapping("/partition")
    public ResponseEntity<Map<String, Object>> sendToPartition(
            @RequestParam String topic,
            @RequestParam int partition,
            @RequestParam String key,
            @RequestParam String message) {

        kafkaProducerService.sendToPartition(topic, partition, key, message);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Message sent to specific partition");
        response.put("topic", topic);
        response.put("partition", partition);
        response.put("key", key);

        return ResponseEntity.ok(response);
    }

    /**
     * Test: Send batch of messages
     * POST /kafka/test/batch?count=10
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> sendBatch(@RequestParam(defaultValue = "10") int count) {

        List<UserEvent> events = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(UserEvent.created(
                    (long) i,
                    "user" + i,
                    "user" + i + "@example.com",
                    20 + i
            ));
        }

        kafkaProducerService.sendEventBatch(KafkaTopics.USER_CREATED, events);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Batch sent");
        response.put("count", count);
        response.put("topic", KafkaTopics.USER_CREATED);

        return ResponseEntity.ok(response);
    }

    /**
     * Test: Trigger DLQ (send invalid message that will fail processing)
     * POST /kafka/test/trigger-dlq
     */
    @PostMapping("/trigger-dlq")
    public ResponseEntity<Map<String, Object>> triggerDLQ() {
        // Send a message that might cause processing failure
        // This is for testing the DLQ mechanism

        Map<String, Object> invalidMessage = new HashMap<>();
        invalidMessage.put("invalid", true);
        invalidMessage.put("test", "This message should fail processing");

        kafkaProducerService.sendAsync(KafkaTopics.USER_CREATED, invalidMessage);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Invalid message sent - may trigger DLQ");
        response.put("topic", KafkaTopics.USER_CREATED);

        return ResponseEntity.ok(response);
    }

    // ==================== Full Flow Test ====================

    /**
     * Test: Full e-commerce flow (User -> Order -> Payment)
     * POST /kafka/test/full-flow?username=john&email=john@example.com
     */
    @PostMapping("/full-flow")
    public ResponseEntity<Map<String, Object>> testFullFlow(
            @RequestParam String username,
            @RequestParam String email) {

        Long userId = System.currentTimeMillis();
        Long orderId = System.currentTimeMillis() + 1;
        Long paymentId = System.currentTimeMillis() + 2;
        BigDecimal amount = new BigDecimal("99.99");

        // Step 1: Create user
        UserEvent userEvent = UserEvent.created(userId, username, email, 25);
        kafkaProducerService.sendEventAsync(KafkaTopics.USER_CREATED, userEvent);

        // Step 2: Create order
        OrderEvent orderEvent = OrderEvent.created(orderId, userId,
                List.of(new OrderEvent.OrderItem(1L, "Test Product", 1, amount)), amount);
        orderEvent.setCorrelationId(userEvent.getCorrelationId()); // Link events
        kafkaProducerService.sendEventAsync(KafkaTopics.ORDER_CREATED, orderEvent);

        // Step 3: Initiate payment
        PaymentEvent paymentEvent = PaymentEvent.initiated(paymentId, orderId, userId, amount, "USD", "CREDIT_CARD");
        paymentEvent.setCorrelationId(userEvent.getCorrelationId()); // Link events
        kafkaProducerService.sendEventAsync(KafkaTopics.PAYMENT_INITIATED, paymentEvent);

        // Step 4: Complete payment (triggers order completion via event chain)
        PaymentEvent paymentComplete = PaymentEvent.completed(paymentId, orderId, "TXN-" + System.currentTimeMillis());
        paymentComplete.setCorrelationId(userEvent.getCorrelationId());
        paymentComplete.setUserId(userId);
        paymentComplete.setAmount(amount);
        kafkaProducerService.sendEventAsync(KafkaTopics.PAYMENT_COMPLETED, paymentComplete);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Full flow events sent");
        response.put("correlationId", userEvent.getCorrelationId());
        response.put("userId", userId);
        response.put("orderId", orderId);
        response.put("paymentId", paymentId);
        response.put("events", List.of(
                "USER_CREATED -> user-created",
                "ORDER_CREATED -> order-created",
                "PAYMENT_INITIATED -> payment-initiated",
                "PAYMENT_COMPLETED -> payment-completed (triggers ORDER_COMPLETED)"
        ));

        return ResponseEntity.ok(response);
    }

    // ==================== Helper ====================

    private Map<String, Object> buildResponse(String message, Object event) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", message);
        response.put("event", event);
        return response;
    }
}
