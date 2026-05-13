package com.cventro.notificationService.service.notificationSuccess;

import com.cventro.notificationService.enums.ScheduledType;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;

public interface NotificationSuccessStrategy {
    boolean supports(ScheduledType scheduleType);

    void apply(Update update, LocalDateTime triggeredAt, NotificationSuccessContext context);
}
