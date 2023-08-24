package com.emmanuel.sarabrandserver.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperBean {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
