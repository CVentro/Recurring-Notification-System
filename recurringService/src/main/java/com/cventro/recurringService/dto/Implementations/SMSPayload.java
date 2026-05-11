package com.cventro.recurringService.dto.Implementations;

import com.cventro.recurringService.dto.PayLoad;
import com.cventro.recurringService.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SMSPayload implements PayLoad {


    @NotBlank(message = "SMS message is required")
    @Size(max = 160, message = "SMS message must be at most 160 characters")
    private  String message;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Phone number must be valid")
    private  String phoneNumber;

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }
}
