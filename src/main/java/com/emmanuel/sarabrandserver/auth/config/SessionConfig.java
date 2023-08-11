package com.emmanuel.sarabrandserver.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Configuration
public class SessionConfig {

    private final FindByIndexNameSessionRepository<? extends Session> indexedRepository;

    public SessionConfig(FindByIndexNameSessionRepository<? extends Session> indexedRepository) {
        this.indexedRepository = indexedRepository;
    }

    /**
     * Maintains a registry of Session information instances. For better understanding visit
     * <a href="https://github.com/spring-projects/spring-session/blob/main/spring-session-docs/modules/ROOT/examples/java/docs/security/SecurityConfiguration.java">...</a>
     * **/
    @Bean
    public SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(indexedRepository);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * A SecurityContextRepository implementation which stores the security context in the HttpSession between requests.
     * This is called in AuthService
     * */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}
