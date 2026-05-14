package com.cventro.notificationService.metrics;

import com.cventro.notificationService.enums.NotificationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackNotificationMetrics {
    NotificationType type();
}
