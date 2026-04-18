package com.cventro.notificationService.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ConsumerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "notifications.email.main")
    public void consumeMain(String message) {
        System.out.println("MAIN received: " + message);

        kafkaTemplate.send("notifications.email.retry.1m", message + "|1");
    }

    @KafkaListener(topics = "notifications.email.retry.1m")
    public void retry1m(String message) {
        System.out.println("RETRY 1M received: " + message);

        kafkaTemplate.send("notifications.email.retry.5m", message + "|2");
    }

    @KafkaListener(topics = "notifications.email.retry.5m")
    public void retry5m(String message) {
        System.out.println("RETRY 5M received: " + message);

        kafkaTemplate.send("notifications.email.dlq", message + "|DLQ");
    }

    @KafkaListener(topics = "notifications.email.dlq")
    public void dlq(String message) {
        System.out.println("DLQ received: " + message);
    }
}