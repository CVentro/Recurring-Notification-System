package com.cventro.recurringService.dto;

import com.cventro.recurringService.dto.Implementations.EmailPayload;
import com.cventro.recurringService.dto.Implementations.SMSPayload;
import com.cventro.recurringService.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailPayload.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = SMSPayload.class, name = "SMS")
})
public interface PayLoad {
    NotificationType getType();
}
