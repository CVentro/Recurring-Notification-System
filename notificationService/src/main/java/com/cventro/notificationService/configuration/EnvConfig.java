package com.cventro.notificationService.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(".")
            .load();

    public static String getEnvValue(String key) {
        return dotenv.get(key);
    }
}