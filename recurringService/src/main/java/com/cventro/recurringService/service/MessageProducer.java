package com.cventro.recurringService.service;

import com.cventro.recurringService.dto.KafkaPayload;
import com.cventro.recurringService.entity.NotificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, KafkaPayload> kafkaTemplate;

    public void sendMessage(NotificationEvent event) {
        KafkaPayload payload = KafkaPayload.builder()
                .eventId(event.getEventId())
                .notificationType(event.getType())
                .scheduledType(event.getScheduleType())
                .build();

        kafkaTemplate.send("notifications.email.main", payload);
    }

}
