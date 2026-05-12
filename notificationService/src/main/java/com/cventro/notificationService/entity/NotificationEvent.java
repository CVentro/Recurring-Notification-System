package com.cventro.notificationService.entity;

import com.cventro.notificationService.dto.PayLoad;
import com.cventro.notificationService.enums.NotificationType;
import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.enums.Status;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
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

    // FIXED | RECURRING | FIXED_RECURRING
    @NotNull(message = "Schedule type is required")
    private ScheduledType scheduleType;

    // Flexible payload
    @Valid
    @NotNull(message = "Payload is required")
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = com.cventro.notificationService.dto.Implementations.EmailPayload.class, name = "EMAIL"),
            @JsonSubTypes.Type(value = com.cventro.notificationService.dto.Implementations.SMSPayload.class, name = "SMS")
    })
    private PayLoad payload;

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
