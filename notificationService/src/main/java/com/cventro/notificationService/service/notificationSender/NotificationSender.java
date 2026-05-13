package com.cventro.notificationService.service.notificationSender;

import com.cventro.notificationService.dto.KafkaPayload;
import com.cventro.notificationService.enums.NotificationType;

public interface NotificationSender {
    boolean supports(NotificationType notificationType);

    void send(KafkaPayload message);
}
