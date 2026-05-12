package com.cventro.recurringService.entity;

import com.cventro.recurringService.enums.NotificationType;
import com.cventro.recurringService.enums.ScheduledType;
import com.cventro.recurringService.enums.Status;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "User id is required")
    private String userId;

    // EMAIL | SMS | PUSH
    @NotNull(message = "Notification type is required")
    private NotificationType type;

    // ONE_TIME | RECURRING | FIXED_COUNT
    @NotNull(message = "Schedule type is required")
    private ScheduledType scheduleType;

    // Flexible payload
    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;

    private Status status;

    private LocalDateTime lastTriggerTime;

    // Interval - Interval between the recurring notifications
    private long intervalMs;

    private long sentCount;
    private long maxCount;

    // Parameters for Retry Logic
    private long retryCount;
    private long maxRetryCount;
    private LocalDateTime lastRetryTime;
    private long retryBackoffMs;

    private LocalDateTime createdAt;
    private LocalDateTime expireAt;

    @AssertTrue(message = "maxCount must be greater than 0 for FIXED_RECURRING events")
    public boolean isMaxCountValidForFixedRecurring() {
        if (scheduleType != ScheduledType.FIXED_RECURRING) {
            return true;
        }
        return maxCount > 0;
    }
}
