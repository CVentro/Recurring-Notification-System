package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.ScheduledType;
import com.cventro.recurringService.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;

@Service
@Slf4j
public class SchedulerService {

    private final MessageProducer messageProducer;

    private final NotificationService notificationService;

    SchedulerService(MessageProducer messageProducer , NotificationService notificationService){
        this.messageProducer = messageProducer;
        this.notificationService = notificationService;
    }


    @Scheduled(fixedRate = 10000)
    public void scheduledNotification(){
        log.info("Finding notifications to publish");

        List<NotificationEvent> listofCreatedNotifications =
                notificationService.findByStatuses(asList(Status.CREATED, Status.SCHEDULED));
        log.info("Fetched {} notifications", listofCreatedNotifications.size());

        LocalDateTime now = LocalDateTime.now();
        for (NotificationEvent event : listofCreatedNotifications) {
            if (!shouldPublish(event, now)) {
                continue;
            }
            messageProducer.sendMessage(event);
        }
    }

    private boolean shouldPublish(NotificationEvent event, LocalDateTime now) {
        if (!isRecurringNotification(event)) {
            return true;
        }

        if (event.getLastTriggerTime() == null) {
            return true;
        }

        return Duration.between(event.getLastTriggerTime(), now).toMillis() >= event.getIntervalMs();
    }

    private boolean isRecurringNotification(NotificationEvent event) {
        return event.getScheduleType() == ScheduledType.RECURRING
                || event.getScheduleType() == ScheduledType.FIXED_RECURRING;
    }
}
