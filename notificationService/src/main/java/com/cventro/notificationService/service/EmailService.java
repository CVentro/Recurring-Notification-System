package com.cventro.notificationService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleMail(String to, String subject, String body) {

        log.info("Sending Mail Now to {}" , to);
//        try{
//            SimpleMailMessage message = new SimpleMailMessage();
//
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(body);
//            mailSender.send(message);
//
//            log.info("Email Sent to  {}  successfully" , to);
//        } catch (Exception e){
//            log.error("Sending email failed", e);
//            throw new RuntimeException("Email sending failed", e);
//        }
    }
}
