package com.cventro.recurringService.service;

import com.cventro.recurringService.dto.KafkaPayload;
import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.NotificationType;
import com.cventro.recurringService.enums.ScheduledType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProducerTest {

    @Mock
    private KafkaTemplate<String, KafkaPayload> kafkaTemplate;

    @InjectMocks
    private MessageProducer messageProducer;

    @Test
    void sendMessageBuildsPayloadAndUsesKafkaTemplate() {
        NotificationEvent event = NotificationEvent.builder()
                .eventId("event-1")
                .type(NotificationType.EMAIL)
                .scheduleType(ScheduledType.RECURRING)
                .build();

        messageProducer.sendMessage(event);

        ArgumentCaptor<KafkaPayload> payloadCaptor = ArgumentCaptor.forClass(KafkaPayload.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("notifications.email.main"), payloadCaptor.capture());

        KafkaPayload payload = payloadCaptor.getValue();
        assertEquals("event-1", payload.getEventId());
        assertEquals(NotificationType.EMAIL, payload.getNotificationType());
        assertEquals(ScheduledType.RECURRING, payload.getScheduledType());
    }
}
