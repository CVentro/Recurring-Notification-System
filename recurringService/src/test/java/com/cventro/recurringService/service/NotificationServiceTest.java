package com.cventro.recurringService.service;

import com.cventro.recurringService.configuration.RetryConfig;
import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.NotificationType;
import com.cventro.recurringService.enums.ScheduledType;
import com.cventro.recurringService.enums.Status;
import com.cventro.recurringService.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private AwsAppConfigService awsAppConfigService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotificationEventSetsDefaultsForFixedRecurringEvent() {
        NotificationEvent input = NotificationEvent.builder()
                .userId("user-1")
                .type(NotificationType.EMAIL)
                .scheduleType(ScheduledType.FIXED_RECURRING)
                .maxCount(7)
                .payload(Map.of("subject", "hello", "message", "body"))
                .build();

        when(repository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(awsAppConfigService.getRetryConfig())
                .thenReturn(new RetryConfig(3, 10_000));

        NotificationEvent result = notificationService.createNotificationEvent(input);

        assertNotNull(result.getEventId());
        assertFalse(result.getEventId().isBlank());
        assertEquals(Status.CREATED, result.getStatus());
        assertEquals(0, result.getSentCount());
        assertEquals(0, result.getRetryCount());
        assertEquals(3, result.getMaxRetryCount());
        assertEquals(10_000, result.getRetryBackoffMs());
        assertEquals(7, result.getMaxCount());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getExpireAt());
        assertNull(result.getLastTriggerTime());

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(repository).save(captor.capture());
        assertEquals(Status.CREATED, captor.getValue().getStatus());
    }

    @Test
    void createNotificationEventResetsMaxCountForNonFixedRecurringEvent() {
        NotificationEvent input = NotificationEvent.builder()
                .userId("user-2")
                .type(NotificationType.SMS)
                .scheduleType(ScheduledType.FIXED)
                .maxCount(99)
                .payload(Map.of("phoneNumber", "+919876543210", "message", "test"))
                .build();

        when(repository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(awsAppConfigService.getRetryConfig())
                .thenReturn(new RetryConfig(4, 15_000));

        NotificationEvent result = notificationService.createNotificationEvent(input);

        assertEquals(0, result.getMaxCount());
        assertEquals(4, result.getMaxRetryCount());
        assertEquals(15_000, result.getRetryBackoffMs());
        assertNotNull(result.getEventId());
        assertNotNull(result.getCreatedAt());
    }
}
