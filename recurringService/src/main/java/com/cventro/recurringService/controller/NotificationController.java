package com.cventro.recurringService.controller;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping("/create-event")
    public ResponseEntity<NotificationEvent> createEvent(@Valid @RequestBody NotificationEvent event){
        NotificationEvent newEvent = service.createNotificationEvent(event);
        return ResponseEntity.ok(newEvent);
    }

    @GetMapping("/get-all-events")
    public ResponseEntity<List<NotificationEvent>> getAllEvents(){
        return ResponseEntity.ok(service.getAllNotificationEvents());
    }

    @PostMapping("/get-event")
    public ResponseEntity<NotificationEvent> getEventById(@RequestBody HashMap<String, Object> body){
        String eventId = (String) body.get("eventId");
        return ResponseEntity.ok(service.getNotificationEventById(eventId));
    }

//    @PostMapping("/update-event")
//    public ResponseEntity<NotificationEvent> updateEvent(@RequestBody HashMap<String, Object> body){
//
//        String eventId = (String) body.get("eventId");
//        NotificationEvent updatedEvent = new NotificationEvent();
//
//        if (body.get("status") != null) {
//            updatedEvent.setStatus((String) body.get("status"));
//        }
//
//        if (body.get("sentCount") != null) {
//            updatedEvent.setSentCount((Integer) body.get("sentCount"));
//        }
//
//        if (body.get("retryCount") != null) {
//            updatedEvent.setRetryCount((Integer) body.get("retryCount"));
//        }
//
//        return ResponseEntity.ok(service.updateNotification(eventId, updatedEvent));
//    }

    @DeleteMapping("/delete-event/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        service.deleteNotificationEvent(id);
        return ResponseEntity.noContent().build();
    }
}
