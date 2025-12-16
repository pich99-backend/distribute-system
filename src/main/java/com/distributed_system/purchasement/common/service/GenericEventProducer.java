package com.distributed_system.purchasement.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenericEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(String topic, Object event) {
        kafkaTemplate.send(topic, event);
        System.out.println("Sent event: " + event.getClass().getSimpleName());
    }
}
