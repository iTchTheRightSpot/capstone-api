package com.emmanuel.sarabrandserver;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestConfig {

    // https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/
    static final MySQLContainer<?> container;

    static {
        // Build MySQL container
        container = new MySQLContainer<>("mysql:latest")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");

        // Start container
        container.start();
    }

    @Bean(name = "testMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
