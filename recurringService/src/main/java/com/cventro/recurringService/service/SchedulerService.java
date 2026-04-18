package com.cventro.recurringService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchedulerService {

    private final MessageProducer messageProducer;

    SchedulerService(MessageProducer messageProducer){
        this.messageProducer = messageProducer;
    }


    @Scheduled(fixedRate = 300000000)
    public void scheduledNotification(){
        log.info("Sending Data to Kafka after 30 seconds");
        messageProducer.sendMessage("Message for Kafka");
    }

}
