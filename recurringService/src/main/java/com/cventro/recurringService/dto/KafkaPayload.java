package com.cventro.recurringService.dto;

import com.cventro.recurringService.enums.NotificationType;
import com.cventro.recurringService.enums.ScheduledType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaPayload {
    private String eventId;
    private NotificationType notificationType;
    private ScheduledType scheduledType;
}
