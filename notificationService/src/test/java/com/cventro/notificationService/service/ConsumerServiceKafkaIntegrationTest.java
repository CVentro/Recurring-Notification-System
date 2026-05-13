package com.cventro.notificationService.service;

import com.cventro.notificationService.dto.KafkaPayload;
import com.cventro.notificationService.enums.NotificationType;
import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.service.notificationSender.NotificationSender;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {ConsumerService.class, ConsumerServiceKafkaIntegrationTest.KafkaConsumerTestConfig.class},
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "logging.file.name=target/notificationService-test.log"
        }
)
@EmbeddedKafka(
        partitions = 1,
        topics = {"notifications.email.main", "notifications.email.retry.1m", "notifications.email.retry.5m", "notifications.email.dlq"}
)
class ConsumerServiceKafkaIntegrationTest {

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationSender notificationSender;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void consumeMainProcessesMessageAndMarksNotificationSent() {
        when(notificationSender.supports(NotificationType.EMAIL)).thenReturn(true);

        KafkaPayload payload = KafkaPayload.builder()
                .eventId("event-100")
                .notificationType("EMAIL")
                .scheduledType("RECURRING")
                .build();

        kafkaTemplate.send("notifications.email.main", payload);

        verify(notificationSender, timeout(10000)).send(any(KafkaPayload.class));
        verify(notificationService, timeout(10000))
                .markNotificationSent(eq("event-100"), eq(ScheduledType.RECURRING));
    }

    @TestConfiguration
    @org.springframework.kafka.annotation.EnableKafka
    static class KafkaConsumerTestConfig {
        @Bean
        ConsumerFactory<String, KafkaPayload> consumerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> props = KafkaTestUtils.consumerProps("notification-it-group", "true", embeddedKafkaBroker);
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
            props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, KafkaPayload.class.getName());
            props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
            return new DefaultKafkaConsumerFactory<>(
                    props,
                    new org.apache.kafka.common.serialization.StringDeserializer(),
                    new JsonDeserializer<>(KafkaPayload.class, false)
            );
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, KafkaPayload> kafkaListenerContainerFactory(
                ConsumerFactory<String, KafkaPayload> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, KafkaPayload> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }

        @Bean
        ProducerFactory<String, Object> producerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        List<NotificationSender> notificationSenders(NotificationSender notificationSender) {
            return List.of(notificationSender);
        }
    }
}
