package com.example.sarabrandserver.worker.auth.service;

import com.example.sarabrandserver.dto.LoginDTO;
import com.example.sarabrandserver.exception.DuplicateException;
import com.example.sarabrandserver.response.AuthResponse;
import com.example.sarabrandserver.worker.dto.WorkerRegisterDTO;
import com.example.sarabrandserver.worker.entity.Worker;
import com.example.sarabrandserver.worker.entity.WorkerRole;
import com.example.sarabrandserver.worker.repository.WorkerRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import static com.example.sarabrandserver.enumeration.RoleEnum.WORKER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Service
public class WorkerAuthService {

    @Value(value = "${custom.max.session}")
    private int MAX_SESSION;

    @Value(value = "${custom.cookie.frontend}")
    private String IS_LOGGED_IN;

    @Value(value = "${server.servlet.session.cookie.domain}")
    private String COOKIE_DOMAIN;

    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int COOKIE_MAX_AGE;

    @Value(value = "${server.servlet.session.cookie.path}")
    private String COOKIE_PATH;

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIE_SECURE;

    private final WorkerRepo workerRepo;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authManager;

    private final SecurityContextRepository securityContextRepository;

    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    private final RedisIndexedSessionRepository redisIndexedSessionRepository;

    private final SessionRegistry sessionRegistry;

    public WorkerAuthService(
            WorkerRepo workerRepo,
            PasswordEncoder passwordEncoder,
            @Qualifier(value = "workerAuthManager") AuthenticationManager authManager,
            SecurityContextRepository securityContextRepository,
            RedisIndexedSessionRepository redisIndexedSessionRepository,
            SessionRegistry sessionRegistry
    ) {
        this.workerRepo = workerRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.securityContextRepository = securityContextRepository;
        this.securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        this.redisIndexedSessionRepository = redisIndexedSessionRepository;
        this.sessionRegistry = sessionRegistry;
    }

    public ResponseEntity<?> register(WorkerRegisterDTO dto) {
        if (this.workerRepo.principalExists(dto.email().trim(), dto.username()) > 0) {
            throw new DuplicateException("email or username exists");
        }

        var worker = new Worker(
                dto.name().trim(),
                dto.email().trim(),
                dto.username().trim(),
                passwordEncoder.encode(dto.password()),
                true, true, true, true
        );
        worker.addRole(new WorkerRole(WORKER));

        this.workerRepo.save(worker);
        return new ResponseEntity<>("Registered!", CREATED);
    }

    public ResponseEntity<?> login(LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = this.authManager
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.principal(), dto.password()));

        // Validate max session
        validateMaxSession(authentication);

        // Create a new context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        // Update SecurityContextHolder and Strategy
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        // Build response
        String list = authentication.getAuthorities().stream().map(String::valueOf).toList().toString();

        // Set custom cookie to replace using local storage to keep track of isLogged in. Look auth.service.ts
        Cookie cookie = new Cookie(IS_LOGGED_IN, URLEncoder.encode(list, StandardCharsets.UTF_8));
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath(COOKIE_PATH);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);

        return new ResponseEntity<>(new AuthResponse(dto.principal()), OK);
    }

    private void validateMaxSession(Authentication authentication) {
        if (MAX_SESSION <= 0) {
            return;
        }

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
