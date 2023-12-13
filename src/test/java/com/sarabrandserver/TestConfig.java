package com.sarabrandserver;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

// https://spring.io/blog/2023/06/23/improved-testcontainers-support-in-spring-boot-3-1
@TestConfiguration(proxyBeanMethods = false)
public class TestConfig {

    @Bean
    @ServiceConnection
    @RestartScope
    static MySQLContainer<?> mySQLContainer() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");
    }

}
