package com.emmanuel.sarabrandserver.security;

import com.emmanuel.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;

// https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/authentication/session/ConcurrentSessionControlAuthenticationStrategy.java
@Component(value = "strategy")
public class CustomAuthStrategy implements SessionAuthenticationStrategy {
    private final CustomUtil customUtil;
    private final JdbcIndexedSessionRepository indexNameSessionRepository;
    private final SessionRegistry sessionRegistry;

    public CustomAuthStrategy(
            CustomUtil customUtil,
            JdbcIndexedSessionRepository indexedRepo,
            SessionRegistry sessionRegistry
    ) {
        this.customUtil = customUtil;
        this.indexNameSessionRepository = indexedRepo;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void onAuthentication(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SessionAuthenticationException {
        var principal = (UserDetails) authentication.getPrincipal();
        var sessions = this.sessionRegistry.getAllSessions(principal, false); // List of SessionInfo
        if (sessions.size() >= this.customUtil.getMaxSession()) {
            sessions
                    .stream()
                    .min(Comparator.comparing(SessionInformation::getLastRequest)) // Gets the oldest session
                    .ifPresent(sessionInfo -> this.indexNameSessionRepository.deleteById(sessionInfo.getSessionId()));
        }
    }
}
