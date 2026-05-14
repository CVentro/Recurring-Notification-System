package com.cventro.notificationService.service;

import com.cventro.notificationService.dto.KafkaPayload;
import com.cventro.notificationService.enums.NotificationType;
import com.cventro.notificationService.metrics.TrackNotificationMetrics;
import com.cventro.notificationService.service.notificationSender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@TrackNotificationMetrics(type = NotificationType.SMS)
public class SmsService implements NotificationSender {

    @Override
    public boolean supports(NotificationType notificationType) {
        return notificationType == NotificationType.SMS;
    }

    @Override
    public void send(KafkaPayload message) {
        log.info("Sending SMS for eventId={}", message.getEventId());
        throw new RuntimeException("Manual SMS failure for retry testing");
    }
}
