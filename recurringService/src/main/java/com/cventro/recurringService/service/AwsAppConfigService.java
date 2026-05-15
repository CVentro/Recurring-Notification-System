package com.cventro.recurringService.service;

import com.cventro.recurringService.configuration.RetryConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse;

@Service
@Slf4j
public class AwsAppConfigService {

    private final AppConfigDataClient appConfigDataClient;
    private final ObjectMapper objectMapper;

    private String configurationToken;
    private RetryConfig cachedConfig = new RetryConfig(1, 1000);

    public AwsAppConfigService(
            AppConfigDataClient appConfigDataClient,
            ObjectMapper objectMapper
    ) {
        this.appConfigDataClient = appConfigDataClient;
        this.objectMapper = objectMapper;
    }

    public RetryConfig getRetryConfig() {
        try {
            if (configurationToken == null) {
                StartConfigurationSessionResponse sessionResponse = appConfigDataClient.startConfigurationSession(
                        StartConfigurationSessionRequest.builder()
                                .applicationIdentifier("RecurringNotificationService")
                                .environmentIdentifier("DEV")
                                .configurationProfileIdentifier("NotificationService-Config")
                                .requiredMinimumPollIntervalInSeconds(30)
                                .build()
                );

                configurationToken = sessionResponse.initialConfigurationToken();
            }

            GetLatestConfigurationResponse latestConfigResponse = appConfigDataClient.getLatestConfiguration(
                    GetLatestConfigurationRequest.builder()
                            .configurationToken(configurationToken)
                            .build()
            );

            configurationToken = latestConfigResponse.nextPollConfigurationToken();
            SdkBytes configBytes = latestConfigResponse.configuration();

            if (configBytes != null && configBytes.asByteArray().length > 0) {
                String json = configBytes.asUtf8String();
                cachedConfig = objectMapper.readValue(json, RetryConfig.class);
            }

            log.info("Cached Message  {}" , cachedConfig);
            return cachedConfig;
        } catch (Exception exception) {
            return cachedConfig;
        }
    }
}
