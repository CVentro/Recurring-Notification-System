package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
        LocalDateTime now = LocalDateTime.now();
        log.info("Finding due notifications to publish at {}", now);

        List<NotificationEvent> dueEvents = notificationService.findDueCreatedEvents(now);
        log.info("Due events count: {}", dueEvents.size());

        for (NotificationEvent event : dueEvents) {
            messageProducer.sendMessage(event);
            notificationService.markTriggered(event.getEventId(), now);
        }
    }

}
