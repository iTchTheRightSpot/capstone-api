package dev.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

class ApplicationTest {

    public static void main(String... args) {
        SpringApplication
                .from(Application::main)
                .with(TestConfig.class, TestController.class)
                .run(args);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        static final Logger log = LoggerFactory.getLogger(TestConfig.class);

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        @ServiceConnection
        static MySQLContainer<?> mySQLContainer() {
            try (final var sql = new MySQLContainer<>("mysql:8.0")) {
                return sql.withDatabaseName("capstone_db")
                        .withUsername("capstone")
                        .withPassword("capstone");
            } catch (RuntimeException ex) {
                log.error("failed to start up MySQL in test/dev mode");
                throw new RuntimeException();
            }
        }

    }
}