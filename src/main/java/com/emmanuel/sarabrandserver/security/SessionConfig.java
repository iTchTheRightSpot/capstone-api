package com.emmanuel.sarabrandserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.Session;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

/** <a href="https://docs.spring.io/spring-session/reference/api.html">...</a> */
@Configuration
@EnableJdbcHttpSession
public class SessionConfig {

    /**
     * Maintains a registry of Session information instances. For better understanding visit
     * <a href="https://github.com/spring-projects/spring-session/blob/main/spring-session-docs/modules/ROOT/examples/java/docs/security/SecurityConfiguration.java">...</a>
     * **/
    @Bean(name = "sessionRegistry")
    public SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry(
            JdbcIndexedSessionRepository indexNameSessionRepository
    ) {
        return new SpringSessionBackedSessionRegistry<>(indexNameSessionRepository);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /** A SecurityContextRepository implementation which stores the security context in the HttpSession between requests. */
    @Bean(name = "contextRepository")
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}
