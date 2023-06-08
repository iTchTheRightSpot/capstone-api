package com.example.sarabrandserver.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component(value = "customStrategy")
public class CustomStrategy implements SessionAuthenticationStrategy {

    @Value(value = "${custom.max.session}")
    private int MAX_SESSION;

    private final RedisIndexedSessionRepository redisIndexedSessionRepository;

    private final SessionRegistry sessionRegistry;

    public CustomStrategy(RedisIndexedSessionRepository redisIndexedSessionRepository, SessionRegistry sessionRegistry) {
        this.redisIndexedSessionRepository = redisIndexedSessionRepository;
        this.sessionRegistry = sessionRegistry;
    }

    public void setMAX_SESSION(int val) {
        this.MAX_SESSION = val;
    }

    @Override
    public void onAuthentication(
            Authentication authentication,
            HttpServletRequest req,
            HttpServletResponse res
    ) throws SessionAuthenticationException {
        var principal = (UserDetails) authentication.getPrincipal();
        List<SessionInformation> sessions = this.sessionRegistry.getAllSessions(principal, false);

        if (sessions.size() >= MAX_SESSION) {
            sessions.stream() //
                    // Gets the oldest session
                    .min(Comparator.comparing(SessionInformation::getLastRequest)) //
                    .ifPresent(sessionInfo -> this.redisIndexedSessionRepository.deleteById(sessionInfo.getSessionId()));
        }
    }

}
