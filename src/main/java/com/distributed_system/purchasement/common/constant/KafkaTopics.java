package com.distributed_system.purchasement.common.constant;

/**
 * Centralized Kafka topic names
 * This makes it easy to manage and change topic names across the application
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Prevent instantiation
    }

    // ==================== User Topics ====================
    public static final String USER_CREATED = "user-created";
    public static final String USER_UPDATED = "user-updated";
    public static final String USER_DELETED = "user-deleted";

    // ==================== Order Topics ====================
    public static final String ORDER_CREATED = "order-created";
    public static final String ORDER_UPDATED = "order-updated";
    public static final String ORDER_CANCELLED = "order-cancelled";
    public static final String ORDER_COMPLETED = "order-completed";

    // ==================== Payment Topics ====================
    public static final String PAYMENT_INITIATED = "payment-initiated";
    public static final String PAYMENT_COMPLETED = "payment-completed";
    public static final String PAYMENT_FAILED = "payment-failed";

    // ==================== Notification Topics ====================
    public static final String NOTIFICATION_EMAIL = "notification-email";
    public static final String NOTIFICATION_SMS = "notification-sms";

    // ==================== Dead Letter Queue Topics ====================
    public static final String DLQ_PREFIX = "dlq-";
    public static final String DLQ_USER = DLQ_PREFIX + "user";
    public static final String DLQ_ORDER = DLQ_PREFIX + "order";
    public static final String DLQ_PAYMENT = DLQ_PREFIX + "payment";

    // ==================== Consumer Groups ====================
    public static final String GROUP_USER_SERVICE = "user-service-group";
    public static final String GROUP_ORDER_SERVICE = "order-service-group";
    public static final String GROUP_PAYMENT_SERVICE = "payment-service-group";
    public static final String GROUP_NOTIFICATION_SERVICE = "notification-service-group";
    public static final String GROUP_DLQ_HANDLER = "dlq-handler-group";
}

