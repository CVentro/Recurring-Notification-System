package com.cventro.recurringService.dto;

import com.cventro.recurringService.dto.Implementations.EmailPayload;
import com.cventro.recurringService.dto.Implementations.SMSPayload;
import com.cventro.recurringService.enums.NotificationType;
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
