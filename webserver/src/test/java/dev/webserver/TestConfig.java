package dev.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

// https://spring.io/blog/2023/06/23/improved-testcontainers-support-in-spring-boot-3-1
@TestConfiguration(proxyBeanMethods = false)
public class TestConfig {

    static final Logger log = LoggerFactory.getLogger(TestConfig.class);

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ServiceConnection
//    @RestartScope
    static MySQLContainer<?> mySQLContainer() {
        try (var sql = new MySQLContainer<>("mysql:8.0")) {
            return sql.withDatabaseName("capstone_db")
                    .withUsername("capstone")
                    .withPassword("capstone");
        } catch (RuntimeException ex) {
            log.error("failed to start up MySQL in test/dev mode");
            throw new RuntimeException();
        }
    }

}
