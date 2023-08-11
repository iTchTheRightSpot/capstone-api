package com.emmanuel.sarabrandserver.auth.bruteforce;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class BruteConfig {

    /**
     * For each authentication that succeeds or fails, a AuthenticationSuccessEvent or AuthenticationFailureEvent,
     * respectively, is fired.
     * <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/events.html">...</a>
     * */
    @Bean(name = "authenticationEventPublisher")
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    @Bean
    public Map<String, Object> mapOperation() {
        return new ConcurrentHashMap<>();
    }

}
