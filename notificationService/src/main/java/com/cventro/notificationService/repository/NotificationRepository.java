package com.cventro.notificationService.repository;

import com.cventro.notificationService.entity.NotificationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEvent,String> {
}
