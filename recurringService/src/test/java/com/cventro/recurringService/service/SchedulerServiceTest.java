package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SchedulerService schedulerService;

    @Test
    void scheduledNotificationPublishesAllCreatedAndScheduledEvents() {
        NotificationEvent event1 = NotificationEvent.builder().eventId("event-1").status(Status.CREATED).build();
        NotificationEvent event2 = NotificationEvent.builder().eventId("event-2").status(Status.SCHEDULED).build();

        when(notificationService.findByStatuses(anyList())).thenReturn(List.of(event1, event2));

        schedulerService.scheduledNotification();

        ArgumentCaptor<List<Status>> statusCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).findByStatuses(statusCaptor.capture());
        assertEquals(List.of(Status.CREATED, Status.SCHEDULED), statusCaptor.getValue());

        verify(messageProducer).sendMessage(event1);
        verify(messageProducer).sendMessage(event2);
    }

    @Test
    void scheduledNotificationDoesNotPublishWhenNoEventsFound() {
        when(notificationService.findByStatuses(anyList())).thenReturn(List.of());

        schedulerService.scheduledNotification();

        verify(messageProducer, never()).sendMessage(org.mockito.ArgumentMatchers.any(NotificationEvent.class));
    }
}
