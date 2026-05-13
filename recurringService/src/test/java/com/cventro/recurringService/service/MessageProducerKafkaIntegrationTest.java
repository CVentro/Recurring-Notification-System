package com.cventro.recurringService.service;

import com.cventro.recurringService.dto.KafkaPayload;
import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.NotificationType;
import com.cventro.recurringService.enums.ScheduledType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = {MessageProducer.class, MessageProducerKafkaIntegrationTest.KafkaProducerTestConfig.class},
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "logging.file.name=target/recurringService-test.log"
        }
)
@EmbeddedKafka(partitions = 1, topics = {"notifications.email.main"})
@DirtiesContext
class MessageProducerKafkaIntegrationTest {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void sendMessagePublishesPayloadToKafkaTopic() {
        NotificationEvent event = NotificationEvent.builder()
                .eventId("event-42")
                .type(NotificationType.EMAIL)
                .scheduleType(ScheduledType.RECURRING)
                .build();

        messageProducer.sendMessage(event);

        Consumer<String, KafkaPayload> consumer = buildKafkaPayloadConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "notifications.email.main");

        var record = KafkaTestUtils.getSingleRecord(consumer, "notifications.email.main", Duration.ofSeconds(10));
        KafkaPayload payload = record.value();

        assertNotNull(payload);
        assertEquals("event-42", payload.getEventId());
        assertEquals(NotificationType.EMAIL, payload.getNotificationType());
        assertEquals(ScheduledType.RECURRING, payload.getScheduledType());
        consumer.close();
    }

    private Consumer<String, KafkaPayload> buildKafkaPayloadConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("producer-it-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<KafkaPayload> jsonDeserializer = new JsonDeserializer<>(KafkaPayload.class);
        jsonDeserializer.addTrustedPackages("*");

        ConsumerFactory<String, KafkaPayload> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                jsonDeserializer
        );
        return consumerFactory.createConsumer();
    }

    @TestConfiguration
    static class KafkaProducerTestConfig {
        @Bean
        ProducerFactory<String, KafkaPayload> producerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
            return new DefaultKafkaProducerFactory<>(producerProps, new org.apache.kafka.common.serialization.StringSerializer(), new JsonSerializer<>());
        }

        @Bean
        KafkaTemplate<String, KafkaPayload> kafkaTemplate(ProducerFactory<String, KafkaPayload> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }
    }
}
