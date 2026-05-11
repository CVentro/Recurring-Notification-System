package com.cventro.recurringService.repository;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEvent , String> {
    List<NotificationEvent> findByStatus(Status status);
}
