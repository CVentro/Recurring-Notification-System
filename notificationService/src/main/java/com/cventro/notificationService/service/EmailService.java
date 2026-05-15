package com.cventro.notificationService.service;

import com.cventro.notificationService.dto.KafkaPayload;
import com.cventro.notificationService.dto.Implementations.EmailPayload;
import com.cventro.notificationService.enums.NotificationType;
import com.cventro.notificationService.metrics.TrackNotificationMetrics;
import com.cventro.notificationService.service.notificationSender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@TrackNotificationMetrics(type = NotificationType.EMAIL)
public class EmailService implements NotificationSender {

    private final JavaMailSender mailSender;
    private final NotificationService notificationService;

    public EmailService(JavaMailSender mailSender, NotificationService notificationService) {
        this.mailSender = mailSender;
        this.notificationService = notificationService;
    }

    public void sendSimpleMail(String to, String subject, String body) {

        log.info("Sending Mail Now to {}" , to);
        try{
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Email Sent to  {}  successfully" , to);
        } catch (Exception e){
            log.error("Sending email failed", e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    @Override
    public boolean supports(NotificationType notificationType) {
        return notificationType == NotificationType.EMAIL;
    }

    @Override
    public void send(KafkaPayload message) {
        EmailPayload emailPayload = notificationService.getEmailPayload(message.getEventId());
        sendSimpleMail(emailPayload.getEmail(), emailPayload.getSubject(), emailPayload.getBody());
    }
}
