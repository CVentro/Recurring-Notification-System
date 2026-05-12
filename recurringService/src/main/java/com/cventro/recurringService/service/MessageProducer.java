package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendMessage(NotificationEvent event) {
        try{
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("notifications.email.main", message);
        }
        catch (JsonProcessingException e){
            throw new RuntimeException("Failed to serialize notification event for Kafka",e);
        }

    }

}