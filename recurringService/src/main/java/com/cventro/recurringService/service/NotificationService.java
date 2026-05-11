package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.Status;
import com.cventro.recurringService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        event.setMaxCount(0);
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
