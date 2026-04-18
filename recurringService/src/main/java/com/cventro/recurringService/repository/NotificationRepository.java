package com.cventro.recurringService.repository;

import com.cventro.recurringService.entity.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEvent , String> {
}
