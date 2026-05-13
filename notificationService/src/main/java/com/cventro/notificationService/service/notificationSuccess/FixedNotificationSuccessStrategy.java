package com.cventro.notificationService.service.notificationSuccess;

import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.enums.Status;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FixedNotificationSuccessStrategy implements NotificationSuccessStrategy {

    @Override
    public boolean supports(ScheduledType scheduleType) {
        return scheduleType == ScheduledType.FIXED;
    }

    @Override
    public void apply(Update update, LocalDateTime triggeredAt) {
        update.set("status", Status.CANCELLED);
    }
}
