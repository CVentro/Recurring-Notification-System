package com.cventro.notificationService.dto;

import com.cventro.notificationService.dto.Implementations.EmailPayload;
import com.cventro.notificationService.dto.Implementations.SMSPayload;
import com.cventro.notificationService.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailPayload.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = SMSPayload.class, name = "SMS")
})
public interface PayLoad {
    @JsonIgnore
    NotificationType getType();
}
