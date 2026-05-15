package com.cventro.notificationService.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Component
public class AwsSecretsManagerConfig {

    private final ObjectMapper objectMapper;
    private final String region;
    private final String mailSecretName;
    private JsonNode mailSecrets;

    public AwsSecretsManagerConfig(
            ObjectMapper objectMapper,
            @Value("${aws.region}") String region,
            @Value("${aws.secretsmanager.mail-secret-name}") String mailSecretName
    ) {
        this.objectMapper = objectMapper;
        this.region = region;
        this.mailSecretName = mailSecretName;
    }

    @PostConstruct
    public void loadMailSecrets() {
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build()) {

            GetSecretValueResponse response = client.getSecretValue(GetSecretValueRequest.builder()
                    .secretId(mailSecretName)
                    .build());

            mailSecrets = objectMapper.readTree(response.secretString());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load mail secrets from AWS Secrets Manager", exception);
        }
    }

    public String getMailSecretValue(String key) {
        JsonNode value = mailSecrets.get(key);
        if (value == null || value.asText().isBlank()) {
            throw new IllegalStateException("Missing required mail secret key: " + key);
        }
        return value.asText();
    }
}
