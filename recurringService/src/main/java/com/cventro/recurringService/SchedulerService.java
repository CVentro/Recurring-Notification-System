package com.cventro.recurringService;

import com.cventro.recurringService.service.MessageProducer;
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


    @Scheduled(fixedRate = 5000)
    public void scheduledNotification(){
        log.info("Sending Data to Kafka after 10 seconds");
        messageProducer.sendMessage("Message for Kafka");
    }

}
