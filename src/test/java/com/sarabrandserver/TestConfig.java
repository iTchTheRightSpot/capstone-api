package com.sarabrandserver;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
public class TestConfig {

    // https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/
    public static final MySQLContainer<?> container;

    static {
        // Build MySQL container
        container = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");

        // Start container
        container.start();
    }

}
