package com.cventro.notificationService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EmailService emailService;

    public ConsumerService(KafkaTemplate<String, String> kafkaTemplate , EmailService emailService) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailService = emailService;
    }

    @KafkaListener(topics = "notifications.email.main")
    public void consumeMain(String message) {
        log.info("Message Recieved in Main Topic : {}" , message);

        try{
            emailService.sendSimpleMail("b321056@iiit-bh.ac.in" , "Test Fail from Rohan" , "Body is not there , Please proceed to coding");
        }
        catch (Exception c){
            log.error("Error Recieved from Email Service : " , c);
        }

//        kafkaTemplate.send("notifications.email.retry.1m", message + "|1");
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