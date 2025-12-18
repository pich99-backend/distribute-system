package com.distributed_system.purchasement.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Kafka Configuration with:
 * - Producer with retries and idempotence
 * - Consumer with manual acknowledgment
 * - Error handling with retry and DLQ
 * - Batch processing support
 */
@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:purchasement-group}")
    private String consumerGroupId;

    // ==================== Producer Configuration ====================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Bootstrap servers
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serializers
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Add type information to headers for proper deserialization
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        // Reliability settings
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry 3 times
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // 1 second between retries

        // Idempotence - ensures exactly-once delivery
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Performance tuning
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batch
        config.put(ProducerConfig.LINGER_MS_CONFIG, 5); // Wait 5ms for more messages
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compress messages

        // Timeout settings
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 2 minutes

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        // Set default topic (optional)
        // template.setDefaultTopic("default-topic");
        return template;
    }

    // ==================== Consumer Configuration ====================

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Bootstrap servers
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Deserializers
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Consumer group
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // Offset management
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from beginning if no offset
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for reliability

        // Performance tuning
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Max records per poll
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes max processing time
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds session timeout
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 seconds heartbeat

        // JSON deserializer settings
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

        // Create deserializer
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>();
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    // ==================== Listener Container Factory ====================

    /**
     * Default listener factory with manual acknowledgment and error handling
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Manual acknowledgment mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Concurrency - number of consumer threads
        factory.setConcurrency(3);

        // Error handler with retry and DLQ
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));

        return factory;
    }

    /**
     * Batch listener factory for high-throughput processing
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> batchKafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true); // Enable batch processing
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));

        return factory;
    }

    /**
     * Listener factory without DLQ (for DLQ consumer itself)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> dlqKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Simple error handler without DLQ (to avoid infinite loop)
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 2L)));

        return factory;
    }

    // ==================== Error Handling ====================

    /**
     * Error handler with retry (3 attempts) and Dead Letter Queue
     */
    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Dead Letter Queue recoverer - sends failed messages to DLQ topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    // Route to DLQ topic: dlq-{original-topic}
                    String dlqTopic = "dlq-" + record.topic();
                    log.error("Sending to DLQ topic: {} due to: {}", dlqTopic, exception.getMessage());
                    return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
                });

        // Retry 3 times with 1 second delay, then send to DLQ
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Log retry attempts
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("Retry attempt {} for topic {} partition {} offset {}",
                    deliveryAttempt, record.topic(), record.partition(), record.offset());
        });

        return errorHandler;
    }
}
