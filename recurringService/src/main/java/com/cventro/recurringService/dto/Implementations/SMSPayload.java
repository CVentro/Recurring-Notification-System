package com.cventro.recurringService.dto.Implementations;

import com.cventro.recurringService.dto.PayLoad;
import com.cventro.recurringService.enums.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SMSPayload implements PayLoad {


    private  String message;
    private  String phoneNumber;

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }
}
