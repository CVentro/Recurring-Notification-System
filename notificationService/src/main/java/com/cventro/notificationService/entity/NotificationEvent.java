package com.cventro.notificationService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class NotificationEvent {
    @Id
    private String eventId;

    private String userId;

    // EMAIL | SMS | PUSH
    private String type;

    // ONE_TIME | RECURRING | FIXED_COUNT
    private String scheduleType;

    // Flexible payload
    private Map<String, Object> payload;

    // CREATED | SCHEDULED | PROCESSING | RETRYING | FAILED | COMPLETED | CANCELLED
    private String status;

    private LocalDateTime lastTriggerTime;
    private String intervalMs;

    private int sentCount;
    private int maxCount;

    private int retryCount;
    private int maxRetryCount;
    private LocalDateTime lastRetryTime;
    private String retryBackoffMs;

    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
}
