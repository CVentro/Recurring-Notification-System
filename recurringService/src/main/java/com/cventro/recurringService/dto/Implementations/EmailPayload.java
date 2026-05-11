package com.cventro.recurringService.dto.Implementations;

import com.cventro.recurringService.dto.PayLoad;
import com.cventro.recurringService.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailPayload implements PayLoad {

     @NotBlank(message = "Email body is required")
     @Size(max = 5000, message = "Email body must be at most 5000 characters")
     private String body;

     @NotBlank(message = "Email subject is required")
     @Size(max = 255, message = "Email subject must be at most 255 characters")
     private String subject;

     @NotBlank(message = "Email address is required")
     @Email(message = "Email address must be valid")
     @Size(max = 320, message = "Email address must be at most 320 characters")
     private String email;

    @Override
    public NotificationType getType(){
        return NotificationType.EMAIL;
    }
}
