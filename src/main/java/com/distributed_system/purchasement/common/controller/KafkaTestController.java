package com.distributed_system.purchasement.common.controller;

import com.distributed_system.purchasement.common.service.GenericEventProducer;
import com.distributed_system.purchasement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka/test")
public class KafkaTestController {

    @Autowired
    private GenericEventProducer producer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/user")
    public String sendUser(@RequestParam String username, @RequestParam int age) {
        kafkaTemplate.send("user-topic", new User(username, age));
        return "UserEvent sent!";
    }

//    @PostMapping("/order")
//    public String sendOrder(@RequestParam int id, @RequestParam String product) {
//        producer.sendEvent("order-topic", new OrderEvent(id, product));
//        return "OrderEvent sent!";
//    }
}
