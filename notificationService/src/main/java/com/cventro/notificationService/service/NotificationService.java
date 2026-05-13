package com.cventro.notificationService.service;

import com.cventro.notificationService.entity.NotificationEvent;
import com.cventro.notificationService.enums.ScheduledType;
import com.cventro.notificationService.repository.NotificationRepository;
import com.cventro.notificationService.service.notificationSuccess.NotificationSuccessContext;
import com.cventro.notificationService.service.notificationSuccess.NotificationSuccessStrategy;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final MongoTemplate mongoTemplate;
    private final List<NotificationSuccessStrategy> notificationSuccessStrategies;


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

    public void markNotificationSent(String eventId, ScheduledType scheduleType) {
        NotificationSuccessStrategy strategy = notificationSuccessStrategies.stream()
                .filter(candidate -> candidate.supports(scheduleType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No success strategy found for schedule type: " + scheduleType));

        Update update = new Update();
        strategy.apply(update, LocalDateTime.now(), getNotificationSuccessContext(eventId, scheduleType));
        mongoTemplate.updateFirst(
                Query.query(where("_id").is(eventId)),
                update,
                NotificationEvent.class
        );
    }

    private NotificationSuccessContext getNotificationSuccessContext(String eventId, ScheduledType scheduleType) {
        if (scheduleType != ScheduledType.FIXED_RECURRING) {
            return NotificationSuccessContext.empty();
        }

        Query query = Query.query(where("_id").is(eventId));
        query.fields().include("sentCount").include("maxCount");

        Document document = mongoTemplate.findOne(query, Document.class, "notifications");
        if (document == null) {
            throw new RuntimeException("Notification Not Found");
        }

        return new NotificationSuccessContext(
                getLongValue(document, "sentCount"),
                getLongValue(document, "maxCount")
        );
    }

    private long getLongValue(Document document, String key) {
        Object value = document.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0;
    }

}
