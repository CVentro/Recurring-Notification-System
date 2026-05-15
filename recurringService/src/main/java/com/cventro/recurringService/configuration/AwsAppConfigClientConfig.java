package com.cventro.recurringService.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;

@Configuration
public class AwsAppConfigClientConfig {

    @Bean
    public AppConfigDataClient appConfigDataClient() {
        return AppConfigDataClient.builder()
                .region(Region.AP_SOUTHEAST_2)
                .build();
    }
}
