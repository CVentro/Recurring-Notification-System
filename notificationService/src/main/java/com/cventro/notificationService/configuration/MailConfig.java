package com.cventro.notificationService.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(EnvConfig.getEnvValue("MAIL_HOST"));
        mailSender.setPort(Integer.parseInt(EnvConfig.getEnvValue("MAIL_PORT")));

        mailSender.setUsername(EnvConfig.getEnvValue("MAIL_USERNAME"));
        mailSender.setPassword(EnvConfig.getEnvValue("MAIL_PASSWORD"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
