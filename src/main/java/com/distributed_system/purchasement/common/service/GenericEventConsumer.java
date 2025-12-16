package com.distributed_system.purchasement.common.service;

import com.distributed_system.purchasement.entity.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GenericEventConsumer {

    @KafkaListener(topics = {"user-topic", "order-topic"}, groupId = "generic-event-group")
    public void listen(Object event) {
        if (event instanceof User userEvent) {
            System.out.println("Received UserEvent: " + userEvent.getUsername());
        } else {
            System.out.println("Received unknown event: " + event);
        }
    }
}
