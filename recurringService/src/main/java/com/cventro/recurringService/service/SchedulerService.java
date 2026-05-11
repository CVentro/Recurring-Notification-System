package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
        log.info("Finding the notifications with Status as CREATED");

        List<NotificationEvent> listofCreatedNotifications =  notificationService.findByStatuses(asList(Status.CREATED , Status.SCHEDULED));
        log.info("List of Created Notifications {}" , listofCreatedNotifications);

        messageProducer.sendMessage("Message for Kafka");
    }

}
