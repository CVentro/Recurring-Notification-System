package com.cventro.recurringService.configuration;

public record RetryConfig(int retryCount, long retryInterval) {
}
