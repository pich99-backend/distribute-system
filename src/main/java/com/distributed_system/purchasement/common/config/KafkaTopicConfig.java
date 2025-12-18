package com.distributed_system.purchasement.common.config;

import com.distributed_system.purchasement.common.constant.KafkaTopics;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Topic Configuration
 * Auto-creates topics on application startup
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Kafka Admin client for topic management
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(config);
    }

    // ==================== User Topics ====================

    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.USER_CREATED)
                .partitions(3)
                .replicas(1) // Use 3 in production with cluster
                .config("retention.ms", "604800000") // 7 days retention
                .config("cleanup.policy", "delete")
                .build();
    }

    @Bean
    public NewTopic userUpdatedTopic() {
        return TopicBuilder.name(KafkaTopics.USER_UPDATED)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic userDeletedTopic() {
        return TopicBuilder.name(KafkaTopics.USER_DELETED)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000")
                .build();
    }

    // ==================== Order Topics ====================

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CREATED)
                .partitions(6) // More partitions for high-volume topic
                .replicas(1)
                .config("retention.ms", "2592000000") // 30 days retention
                .build();
    }

    @Bean
    public NewTopic orderUpdatedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_UPDATED)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CANCELLED)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    @Bean
    public NewTopic orderCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_COMPLETED)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    // ==================== Payment Topics ====================

    @Bean
    public NewTopic paymentInitiatedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_INITIATED)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_COMPLETED)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    // ==================== Notification Topics ====================

    @Bean
    public NewTopic notificationEmailTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_EMAIL)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "86400000") // 1 day retention
                .build();
    }

    @Bean
    public NewTopic notificationSmsTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_SMS)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "86400000")
                .build();
    }

    // ==================== Dead Letter Queue Topics ====================

    @Bean
    public NewTopic dlqUserTopic() {
        return TopicBuilder.name(KafkaTopics.DLQ_USER)
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "2592000000") // 30 days - keep DLQ longer
                .build();
    }

    @Bean
    public NewTopic dlqOrderTopic() {
        return TopicBuilder.name(KafkaTopics.DLQ_ORDER)
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    @Bean
    public NewTopic dlqPaymentTopic() {
        return TopicBuilder.name(KafkaTopics.DLQ_PAYMENT)
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    // ==================== Compacted Topic Example ====================

    /**
     * Compacted topic - keeps only latest value per key
     * Useful for maintaining current state
     */
    @Bean
    public NewTopic userStateTopic() {
        return TopicBuilder.name("user-state")
                .partitions(3)
                .replicas(1)
                .config("cleanup.policy", "compact") // Compacted topic
                .config("min.cleanable.dirty.ratio", "0.5")
                .config("segment.ms", "604800000")
                .build();
    }
}

