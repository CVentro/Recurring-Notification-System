package com.cventro.notificationService.service;

import com.cventro.notificationService.dto.KafkaPayload;
import com.cventro.notificationService.enums.NotificationType;
import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.service.notificationSender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ConsumerService {

    private static final String RETRY_1M_TOPIC = "notifications.email.retry.1m";
    private static final String RETRY_5M_TOPIC = "notifications.email.retry.5m";
    private static final String DLQ_TOPIC = "notifications.email.dlq";

    private final RetryQueueSchedulerService retryQueueSchedulerService;
    private final NotificationService notificationService;
    private final List<NotificationSender> notificationSenders;

    public ConsumerService(RetryQueueSchedulerService retryQueueSchedulerService,
                           NotificationService notificationService,
                           List<NotificationSender> notificationSenders) {
        this.retryQueueSchedulerService = retryQueueSchedulerService;
        this.notificationService = notificationService;
        this.notificationSenders = notificationSenders;
    }

    @KafkaListener(topics = "notifications.email.main")
    public void consumeMain(KafkaPayload message) {
        log.info("Message Recieved in Main Topic : {}", message);
        processNotification(message, RETRY_1M_TOPIC);
    }

    @KafkaListener(topics = "notifications.email.retry.1m")
    public void retry1m(KafkaPayload message) {
        log.info("Retry 1M received: {}", message);
        processNotification(message, RETRY_5M_TOPIC);
    }

    @KafkaListener(topics = "notifications.email.retry.5m")
    public void retry5m(KafkaPayload message) {
        log.info("Retry 5M received: {}", message);
        processNotification(message, DLQ_TOPIC, true);
    }

    @KafkaListener(topics = "notifications.email.dlq")
    public void dlq(KafkaPayload message) {
        log.error("DLQ received: {}", message);
        processNotification(message, DLQ_TOPIC);
    }

    private void processNotification(KafkaPayload message, String failureTopic) {
        processNotification(message, failureTopic, false);
    }

    private void processNotification(KafkaPayload message, String failureTopic, boolean forceDlqOnFailure) {
        try {
            NotificationType notificationType = NotificationType.valueOf(message.getNotificationType());
            NotificationSender sender = notificationSenders.stream()
                    .filter(candidate -> candidate.supports(notificationType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No sender found for notification type: " + notificationType));

            sender.send(message);
            notificationService.markNotificationSent(message.getEventId(), ScheduledType.valueOf(message.getScheduledType()));
        } catch (Exception exception) {
            log.error("Error received while processing notification, sending to {}", failureTopic, exception);
            sendToFailureTopic(message, failureTopic, forceDlqOnFailure);
        }
    }

    private void sendToFailureTopic(KafkaPayload message, String failureTopic, boolean forceDlqOnFailure) {
        boolean retryAllowed = notificationService.markNotificationRetry(message.getEventId());
        if (!retryAllowed) {
            log.info("Notification eventId={} is not eligible for retry yet", message.getEventId());
            return;
        }

        if (forceDlqOnFailure) {
            retryQueueSchedulerService.sendAfterDelay(DLQ_TOPIC, message);
            return;
        }

        retryQueueSchedulerService.sendAfterDelay(failureTopic, message);
    }
}
