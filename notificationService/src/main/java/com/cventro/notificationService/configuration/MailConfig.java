package com.cventro.notificationService.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private final AwsSecretsManagerConfig secretsManagerConfig;

    public MailConfig(AwsSecretsManagerConfig secretsManagerConfig) {
        this.secretsManagerConfig = secretsManagerConfig;
    }

    @Bean
    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(secretsManagerConfig.getMailSecretValue("MAIL_HOST"));
        mailSender.setPort(Integer.parseInt(secretsManagerConfig.getMailSecretValue("MAIL_PORT")));

        mailSender.setUsername(secretsManagerConfig.getMailSecretValue("MAIL_USERNAME"));
        mailSender.setPassword(secretsManagerConfig.getMailSecretValue("MAIL_PASSWORD"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
