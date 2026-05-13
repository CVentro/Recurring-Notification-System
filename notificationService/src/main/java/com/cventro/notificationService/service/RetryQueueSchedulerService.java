package com.cventro.notificationService.service;

import com.cventro.notificationService.dto.KafkaPayload;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RetryQueueSchedulerService {

    private static final Map<String, Duration> TOPIC_DELAYS = Map.of(
            "notifications.email.retry.1m", Duration.ofSeconds(10),
            "notifications.email.retry.5m", Duration.ofSeconds(40),
            "notifications.email.dlq", Duration.ofMinutes(1)
    );

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public RetryQueueSchedulerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAfterDelay(String topic, KafkaPayload message) {
        Duration delay = TOPIC_DELAYS.getOrDefault(topic, Duration.ZERO);
        log.info("Scheduling eventId={} to topic={} after {} ms", message.getEventId(), topic, delay.toMillis());

        scheduler.schedule(
                () -> kafkaTemplate.send(topic, message),
                delay.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
