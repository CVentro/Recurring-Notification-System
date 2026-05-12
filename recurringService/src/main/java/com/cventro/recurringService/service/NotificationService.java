package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.ScheduledType;
import com.cventro.recurringService.enums.Status;
import com.cventro.recurringService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationEvent createNotificationEvent(NotificationEvent event){
        event.setEventId(UUID.randomUUID().toString());
        event.setCreatedAt(LocalDateTime.now());
        event.setExpireAt(LocalDateTime.now());
        event.setLastTriggerTime(LocalDateTime.now());
        event.setStatus(Status.CREATED);
        event.setSentCount(0);
        event.setRetryCount(0);
        event.setMaxRetryCount(3);
        event.setMaxCount(event.getScheduleType() == ScheduledType.FIXED_RECURRING ? event.getMaxCount() : 0);
        event.setRetryBackoffMs(10000);
        return repository.save(event);
    }

    public NotificationEvent getNotificationEventById(String eventId){
        return repository.findById(eventId)
                .orElseThrow( () -> new RuntimeException("Notification Not Found"));
    }

    public List<NotificationEvent> findByStatus(Status status){
        return repository.findByStatus(status);
    }

    public List<NotificationEvent> findDueCreatedEvents(LocalDateTime now){
        List<NotificationEvent> createdEvents = repository.findByStatus(Status.CREATED);
        List<NotificationEvent> dueEvents = new ArrayList<>();

        for(NotificationEvent event : createdEvents){
            if (isDue(event, now)) {
                dueEvents.add(event);
            }
        }
        return dueEvents;
    }

    public NotificationEvent markTriggered(String eventId, LocalDateTime triggerTime) {
        NotificationEvent existing = getNotificationEventById(eventId);
        existing.setLastTriggerTime(triggerTime);
        existing.setSentCount(existing.getSentCount() + 1);
        return repository.save(existing);
    }

    private boolean isDue(NotificationEvent event,LocalDateTime now){
        if(event.getLastTriggerTime()==null){
            return true;
        }
        long intervalMs = event.getIntervalMs();
        if(intervalMs<=0){
            return event.getSentCount()==0;
        }

        LocalDateTime nextTriggerTime = event.getLastTriggerTime().plusNanos(intervalMs * 1_000_000);
        return !now.isBefore(nextTriggerTime);
    }
    public List<NotificationEvent> findByStatuses(List<Status> statuses) {
        return repository.findByStatusIn(statuses);
    }

    public List<NotificationEvent> getAllNotificationEvents(){
        return repository.findAll();
    }

    public void deleteNotificationEvent(String eventId){
        if (!repository.existsById(eventId)) {
            return;
        }
        repository.deleteById(eventId);
    }

    public NotificationEvent stopNotificationEvent(String eventId) {
        NotificationEvent existing = getNotificationEventById(eventId);
        existing.setStatus(Status.CANCELLED);
        existing.setLastTriggerTime(LocalDateTime.now());
        return repository.save(existing);
    }

    public NotificationEvent updateNotification(String eventId, NotificationEvent updatedEvent) {

        NotificationEvent existing = getNotificationEventById(eventId);

        if (updatedEvent.getStatus() != null) {
            existing.setStatus(updatedEvent.getStatus());
        }

        if (updatedEvent.getSentCount() != 0) {
            existing.setSentCount(updatedEvent.getSentCount());
        }

        if (updatedEvent.getRetryCount() != 0) {
            existing.setRetryCount(updatedEvent.getRetryCount());
        }

        existing.setLastTriggerTime(LocalDateTime.now());

        return repository.save(existing);
    }
}
