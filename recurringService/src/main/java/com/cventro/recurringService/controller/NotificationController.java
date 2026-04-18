package com.cventro.recurringService.controller;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping("/create-event")
    public ResponseEntity<NotificationEvent> createEvent(@RequestBody NotificationEvent event){
        NotificationEvent newEvent = service.createNotificationEvent(event);
        return ResponseEntity.ok(newEvent);
    }

    @GetMapping("/get-all-events")
    public ResponseEntity<List<NotificationEvent>> getAllEvents(){
        return ResponseEntity.ok(service.getAllNotificationEvents());
    }

    @GetMapping("/get-event/{id}")
    public ResponseEntity<NotificationEvent> getEventById(@PathVariable("id") String id){
        return ResponseEntity.ok(service.getNotificationEventById(id));
    }

    @PatchMapping("/update-event/{id}")
    public ResponseEntity<NotificationEvent> updateEvent(@PathVariable("id") String id ,
                                                         @RequestBody NotificationEvent newEvent){
        return ResponseEntity.ok(service.updateNotification(id,newEvent));
    }

    @DeleteMapping("/delete-event/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        service.deleteNotificationEvent(id);
        return ResponseEntity.noContent().build();
    }
}
