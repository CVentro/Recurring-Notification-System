package com.cventro.notificationService.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static com.cventro.notificationService.metrics.NotificationMetricsConstants.DELIVERY_LATENCY;
import static com.cventro.notificationService.metrics.NotificationMetricsConstants.DELIVERY_TOTAL;
import static com.cventro.notificationService.metrics.NotificationMetricsConstants.STATUS_FAILURE;
import static com.cventro.notificationService.metrics.NotificationMetricsConstants.STATUS_SUCCESS;
import static com.cventro.notificationService.metrics.NotificationMetricsConstants.TAG_NOTIFICATION_TYPE;
import static com.cventro.notificationService.metrics.NotificationMetricsConstants.TAG_STATUS;

@Aspect
@Component
@RequiredArgsConstructor
public class NotificationMetricsAdvice {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.cventro.notificationService.service.notificationSender.NotificationSender.send(..))"
            + " && @within(trackNotificationMetrics)")
    public Object recordNotificationMetrics(ProceedingJoinPoint joinPoint,
                                            TrackNotificationMetrics trackNotificationMetrics) throws Throwable {
        long startedAt = System.nanoTime();
        String notificationType = trackNotificationMetrics.type().name();

        try {
            Object result = joinPoint.proceed();
            record(notificationType, STATUS_SUCCESS, System.nanoTime() - startedAt);
            return result;
        } catch (Throwable throwable) {
            record(notificationType, STATUS_FAILURE, System.nanoTime() - startedAt);
            throw throwable;
        }
    }

    private void record(String notificationType, String status, long durationNanos) {
        Tags tags = Tags.of(
                TAG_NOTIFICATION_TYPE, notificationType,
                TAG_STATUS, status
        );

        Counter.builder(DELIVERY_TOTAL)
                .description("Notification delivery attempts")
                .tags(tags)
                .register(meterRegistry)
                .increment();

        NotificationMetricsUtil.recordLatency(meterRegistry, DELIVERY_LATENCY, durationNanos, tags);
    }
}
