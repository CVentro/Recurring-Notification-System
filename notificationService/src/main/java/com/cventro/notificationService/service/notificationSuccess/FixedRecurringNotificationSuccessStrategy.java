package com.cventro.notificationService.service.notificationSuccess;

import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.enums.Status;
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
    public void apply(Update update, LocalDateTime triggeredAt, NotificationSuccessContext context) {
        long nextSentCount = context.sentCount() + 1;

        update.set("lastTriggerTime", triggeredAt);
        update.set("status", nextSentCount >= context.maxCount() ? Status.CANCELLED : Status.SCHEDULED);
        update.inc("sentCount", 1);
    }
}
