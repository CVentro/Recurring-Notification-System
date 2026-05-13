package com.cventro.notificationService.service.notificationSuccess;

import com.cventro.notificationService.enums.ScheduledType;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FixedRecurringNotificationSuccessStrategy implements NotificationSuccessStrategy {

    @Override
    public boolean supports(ScheduledType scheduleType) {
        return scheduleType == ScheduledType.FIXED_RECURRING;
    }

    @Override
    public void apply(Update update, LocalDateTime triggeredAt) {
        update.set("lastTriggerTime", triggeredAt);
        update.inc("sentCount", 1);
    }
}
