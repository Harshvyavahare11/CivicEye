package com.civiceye;
 
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.Duration;

/**
 * Main application class for CivicEye Backend with background scheduling enabled.
 */
@SpringBootApplication
@EnableScheduling
public class CivicEyeApplication {

    private static final Logger log = LoggerFactory.getLogger(CivicEyeApplication.class);

    public static void main(String[] args) {
        loadDotenvIfPresent();
        SpringApplication.run(CivicEyeApplication.class, args);
    }

    private static void loadDotenvIfPresent() {
        try {
            File rootEnv = new File("../.env");
            File currentEnv = new File(".env");
            String directory = rootEnv.exists() ? ".." : (currentEnv.exists() ? "." : null);

            if (directory != null) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(directory)
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
                log.info("Loaded environment variables from .env file");
            }
        } catch (Exception e) {
            log.debug("No .env loaded: {}", e.getMessage());
        }
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofSeconds(90))
                .build();
    }
}
