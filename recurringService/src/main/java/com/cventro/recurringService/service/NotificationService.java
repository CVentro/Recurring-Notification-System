package com.cventro.recurringService.service;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationEvent createNotificationEvent(NotificationEvent event){
        event.setEventId(UUID.randomUUID().toString());
        event.setCreatedAt(LocalDateTime.now());
        event.setStatus("CREATED");
        event.setSentCount(0);
        event.setRetryCount(0);
        return repository.save(event);
    }

    public NotificationEvent getNotificationEventById(String eventId){
        return repository.findById(eventId)
                .orElseThrow( () -> new RuntimeException("Notification Not Found"));
    }

    public List<NotificationEvent> getAllNotificationEvents(){
        return repository.findAll();
    }

    public void deleteNotificationEvent(String eventId){
        NotificationEvent existing = getNotificationEventById(eventId);
        repository.delete(existing);
    }

    public NotificationEvent updateNotification(String eventId, NotificationEvent updatedEvent) {

        NotificationEvent existing = getNotificationEventById(eventId);

        existing.setStatus(updatedEvent.getStatus());
        existing.setSentCount(updatedEvent.getSentCount());
        existing.setRetryCount(updatedEvent.getRetryCount());
        existing.setLastTriggerTime(LocalDateTime.now());
        existing.setLastRetryTime(LocalDateTime.now());

        return repository.save(existing);
    }
}