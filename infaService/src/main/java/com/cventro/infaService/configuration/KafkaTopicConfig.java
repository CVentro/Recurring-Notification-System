package com.cventro.infaService.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;

public class KafkaTopicConfig {

    @Bean
    public NewTopic mainTopic() {
        return new NewTopic("notifications.email.main", 3, (short) 1);
    }

    @Bean
    public NewTopic retry1m() {
        return new NewTopic("notifications.email.retry.1m", 3, (short) 1);
    }

    @Bean
    public NewTopic retry5m() {
        return new NewTopic("notifications.email.retry.5m", 3, (short) 1);
    }

    @Bean
    public NewTopic dlq() {
        return new NewTopic("notifications.email.dlq", 3, (short) 1);
    }
}
