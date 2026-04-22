package com.cventro.recurringService.dto.Implementations;

import com.cventro.recurringService.dto.PayLoad;
import com.cventro.recurringService.enums.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailPayload implements PayLoad {

     private String body;
     private String subject;
     private String email;

    @Override
    public NotificationType getType(){
        return NotificationType.EMAIL;
    }
}
