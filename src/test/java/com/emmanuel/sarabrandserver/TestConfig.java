package com.emmanuel.sarabrandserver;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestConfig {

    @Bean(name = "testMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
