package com.cventro.notificationService.service;

import com.cventro.notificationService.dto.Implementations.EmailPayload;
import com.cventro.notificationService.entity.NotificationEvent;
import com.cventro.notificationService.enums.NotificationType;
import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.enums.Status;
import com.cventro.notificationService.repository.NotificationRepository;
import com.cventro.notificationService.service.notificationSuccess.FixedNotificationSuccessStrategy;
import com.cventro.notificationService.service.notificationSuccess.FixedRecurringNotificationSuccessStrategy;
import com.cventro.notificationService.service.notificationSuccess.NotificationSuccessStrategy;
import com.cventro.notificationService.service.notificationSuccess.RecurringNotificationSuccessStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        List<NotificationSuccessStrategy> successStrategies = List.of(
                new FixedNotificationSuccessStrategy(),
                new RecurringNotificationSuccessStrategy(),
                new FixedRecurringNotificationSuccessStrategy()
        );
        notificationService = new NotificationService(
                repository,
                mongoTemplate,
                new ObjectMapper(),
                successStrategies
        );
    }

    @Test
    void getNotificationEventByIdReturnsExistingEvent() {
        NotificationEvent event = NotificationEvent.builder().eventId("event-1").build();
        when(repository.findById("event-1")).thenReturn(Optional.of(event));

        assertThat(notificationService.getNotificationEventById("event-1")).isSameAs(event);
    }

    @Test
    void getNotificationEventByIdThrowsWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationEventById("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Notification Not Found");
    }

    @Test
    void updateNotificationOnlyAppliesProvidedMutableFields() {
        NotificationEvent existing = NotificationEvent.builder()
                .eventId("event-1")
                .status(Status.SCHEDULED)
                .sentCount(1)
                .retryCount(2)
                .build();
        NotificationEvent update = NotificationEvent.builder()
                .status(Status.RETRYING)
                .sentCount(5)
                .retryCount(3)
                .build();

        when(repository.findById("event-1")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        NotificationEvent result = notificationService.updateNotification("event-1", update);

        assertThat(result.getStatus()).isEqualTo(Status.RETRYING);
        assertThat(result.getSentCount()).isEqualTo(5);
        assertThat(result.getRetryCount()).isEqualTo(3);
        assertThat(result.getLastTriggerTime()).isNotNull();
        verify(repository).save(existing);
    }

    @Test
    void updateNotificationKeepsCountersWhenUpdatedCountersAreZero() {
        NotificationEvent existing = NotificationEvent.builder()
                .eventId("event-1")
                .sentCount(4)
                .retryCount(2)
                .build();
        NotificationEvent update = new NotificationEvent();

        when(repository.findById("event-1")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        NotificationEvent result = notificationService.updateNotification("event-1", update);

        assertThat(result.getSentCount()).isEqualTo(4);
        assertThat(result.getRetryCount()).isEqualTo(2);
        assertThat(result.getLastTriggerTime()).isNotNull();
    }

    @Test
    void deleteNotificationEventDeletesExistingEvent() {
        NotificationEvent event = NotificationEvent.builder().eventId("event-1").build();
        when(repository.findById("event-1")).thenReturn(Optional.of(event));

        notificationService.deleteNotificationEvent("event-1");

        verify(repository).delete(event);
    }

    @Test
    void markNotificationSentForFixedNotificationCancelsIt() {
        notificationService.markNotificationSent("event-1", ScheduledType.FIXED);

        Document update = captureUpdate().getUpdateObject();
        assertThat((Document) update.get("$set")).containsEntry("status", Status.CANCELLED);
        assertUpdatedEventId("event-1");
    }

    @Test
    void markNotificationSentForRecurringNotificationSchedulesNextTrigger() {
        notificationService.markNotificationSent("event-1", ScheduledType.RECURRING);

        Document update = captureUpdate().getUpdateObject();

        assertThat((Document) update.get("$set")).containsKey("lastTriggerTime");
        assertThat((Document) update.get("$set")).containsEntry("status", Status.SCHEDULED);
    }

    @Test
    void markNotificationSentForFixedRecurringNotificationSchedulesWhenBelowMaxCount() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("sentCount", 1L).append("maxCount", 3L));

        notificationService.markNotificationSent("event-1", ScheduledType.FIXED_RECURRING);

        Document update = captureUpdate().getUpdateObject();

        assertThat((Document) update.get("$set")).containsKey("lastTriggerTime");
        assertThat((Document) update.get("$set")).containsEntry("status", Status.SCHEDULED);
        assertThat((Document) update.get("$inc")).containsEntry("sentCount", 1);
    }

    @Test
    void markNotificationSentForFixedRecurringNotificationCancelsAtMaxCount() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("sentCount", 2L).append("maxCount", 3L));

        notificationService.markNotificationSent("event-1", ScheduledType.FIXED_RECURRING);

        Document update = captureUpdate().getUpdateObject();

        assertThat((Document) update.get("$set")).containsEntry("status", Status.CANCELLED);
        assertThat((Document) update.get("$inc")).containsEntry("sentCount", 1);
    }

    @Test
    void markNotificationSentThrowsWhenNoStrategySupportsScheduleType() {
        NotificationService serviceWithoutStrategies = new NotificationService(
                repository,
                mongoTemplate,
                new ObjectMapper(),
                List.of()
        );

        assertThatThrownBy(() -> serviceWithoutStrategies.markNotificationSent("event-1", ScheduledType.FIXED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No success strategy found for schedule type: FIXED");
    }

    @Test
    void markNotificationRetryDoesNothingWhenNotificationIsCancelled() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("status", Status.CANCELLED)
                        .append("retryCount", 1L)
                        .append("maxRetryCount", 3L));

        boolean shouldRetry = notificationService.markNotificationRetry("event-1");

        assertThat(shouldRetry).isFalse();
        verify(mongoTemplate, never()).updateFirst(any(Query.class), any(Update.class), eq(NotificationEvent.class));
    }

    @Test
    void markNotificationRetryCancelsWhenMaxRetryCountReached() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("status", Status.RETRYING)
                        .append("retryCount", 3L)
                        .append("maxRetryCount", 3L));

        boolean shouldRetry = notificationService.markNotificationRetry("event-1");

        assertThat(shouldRetry).isFalse();
        assertThat((Document) captureUpdate().getUpdateObject().get("$set")).containsEntry("status", Status.CANCELLED);
    }

    @Test
    void markNotificationRetryIncrementsRetryCountAndMarksRetrying() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("status", "SCHEDULED")
                        .append("retryCount", 1L)
                        .append("maxRetryCount", 3L));

        boolean shouldRetry = notificationService.markNotificationRetry("event-1");

        Document update = captureUpdate().getUpdateObject();

        assertThat(shouldRetry).isTrue();
        Document set = (Document) update.get("$set");
        assertThat(set).containsEntry("retryCount", 2L);
        assertThat(set).containsEntry("status", Status.RETRYING);
        assertThat(set).containsKey("lastRetryTime");
    }

    @Test
    void markNotificationRetryThrowsWhenNotificationIsMissing() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(null);

        assertThatThrownBy(() -> notificationService.markNotificationRetry("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Notification Not Found");
    }

    @Test
    void markNotificationCancelledUpdatesStatus() {
        notificationService.markNotificationCancelled("event-1");

        assertThat((Document) captureUpdate().getUpdateObject().get("$set")).containsEntry("status", Status.CANCELLED);
        assertUpdatedEventId("event-1");
    }

    @Test
    void getEmailPayloadReturnsConvertedPayloadForEmailNotification() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("type", "EMAIL")
                        .append("payload", Map.of(
                                "email", "person@example.com",
                                "subject", "Hello",
                                "body", "Welcome"
                        )));

        EmailPayload payload = notificationService.getEmailPayload("event-1");

        assertThat(payload.getEmail()).isEqualTo("person@example.com");
        assertThat(payload.getSubject()).isEqualTo("Hello");
        assertThat(payload.getBody()).isEqualTo("Welcome");
    }

    @Test
    void getEmailPayloadThrowsWhenNotificationIsMissing() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(null);

        assertThatThrownBy(() -> notificationService.getEmailPayload("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Notification Not Found");
    }

    @Test
    void getEmailPayloadThrowsWhenNotificationIsNotEmail() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("type", NotificationType.SMS.name())
                        .append("payload", Map.of("message", "hello")));

        assertThatThrownBy(() -> notificationService.getEmailPayload("event-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Notification event event-1 is not an email notification");
    }

    @Test
    void getEmailPayloadThrowsWhenPayloadIsMissing() {
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("notifications")))
                .thenReturn(new Document("type", NotificationType.EMAIL.name()));

        assertThatThrownBy(() -> notificationService.getEmailPayload("event-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Notification event event-1 does not contain a payload");
    }

    @Test
    void getAllNotificationEventsDelegatesToRepository() {
        List<NotificationEvent> events = List.of(NotificationEvent.builder().eventId("event-1").build());
        when(repository.findAll()).thenReturn(events);

        assertThat(notificationService.getAllNotificationEvents()).isSameAs(events);
        verifyNoInteractions(mongoTemplate);
    }

    private Update captureUpdate() {
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).updateFirst(any(Query.class), updateCaptor.capture(), eq(NotificationEvent.class));
        return updateCaptor.getValue();
    }

    private void assertUpdatedEventId(String eventId) {
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).updateFirst(queryCaptor.capture(), any(Update.class), eq(NotificationEvent.class));
        assertThat(queryCaptor.getValue().getQueryObject()).containsEntry("_id", eventId);
    }
}
