package com.cventro.notificationService.service;

import com.cventro.notificationService.dto.KafkaPayload;
import com.cventro.notificationService.enums.NotificationType;
import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.service.notificationSender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ConsumerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationService notificationService;
    private final List<NotificationSender> notificationSenders;

    public ConsumerService(KafkaTemplate<String, Object> kafkaTemplate,
                           NotificationService notificationService,
                           List<NotificationSender> notificationSenders) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
        this.notificationSenders = notificationSenders;
    }

    @KafkaListener(topics = "notifications.email.main")
    public void consumeMain(KafkaPayload message) {
        log.info("Message Recieved in Main Topic : {}", message);

        try{
            NotificationType notificationType = NotificationType.valueOf(message.getNotificationType());
            NotificationSender sender = notificationSenders.stream()
                    .filter(candidate -> candidate.supports(notificationType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No sender found for notification type: " + notificationType));

            sender.send(message);
            notificationService.markNotificationSent(message.getEventId(), ScheduledType.valueOf(message.getScheduledType()));
        }
        catch (Exception c){
            log.error("Error Recieved while processing notification : " , c);
        }

//        kafkaTemplate.send("notifications.email.retry.1m", message + "|1");
    }

    @KafkaListener(topics = "notifications.email.retry.1m")
    public void retry1m(KafkaPayload message) {
        System.out.println("RETRY 1M received: " + message);

        kafkaTemplate.send("notifications.email.retry.5m", message);
    }

    @KafkaListener(topics = "notifications.email.retry.5m")
    public void retry5m(KafkaPayload message) {
        System.out.println("RETRY 5M received: " + message);

        kafkaTemplate.send("notifications.email.dlq", message);
    }

    @KafkaListener(topics = "notifications.email.dlq")
    public void dlq(KafkaPayload message) {
        System.out.println("DLQ received: " + message);
    }
}
