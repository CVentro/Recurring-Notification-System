package com.cventro.notificationService.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

public final class NotificationMetricsUtil {

    private NotificationMetricsUtil() {
    }

    public static void recordLatency(MeterRegistry meterRegistry, String metricName, long durationNanos, Tags tags) {
        Timer.builder(metricName)
                .description("Notification delivery latency")
                .tags(tags)
                .register(meterRegistry)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
