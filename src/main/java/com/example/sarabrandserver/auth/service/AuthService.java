package com.example.sarabrandserver.auth.service;

import com.example.sarabrandserver.client.dto.ClientRegisterDTO;
import com.example.sarabrandserver.client.entity.ClientRole;
import com.example.sarabrandserver.client.entity.Clientz;
import com.example.sarabrandserver.client.repository.ClientRepo;
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
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.example.sarabrandserver.enumeration.RoleEnum.CLIENT;
import static com.example.sarabrandserver.enumeration.RoleEnum.WORKER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Service @Setter
public class AuthService {

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

    private final ClientRepo clientRepo;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager clientAuthManager;

    private final AuthenticationManager workerAuthManager;

    private final SecurityContextRepository securityContextRepository;

    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public AuthService(
            WorkerRepo workerRepo,
            ClientRepo clientRepo,
            PasswordEncoder passwordEncoder,
            @Qualifier(value = "clientAuthManager") AuthenticationManager clientAuthManager,
            @Qualifier(value = "workerAuthManager") AuthenticationManager workerAuthManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy
    ) {
        this.workerRepo = workerRepo;
        this.clientRepo = clientRepo;
        this.passwordEncoder = passwordEncoder;
        this.clientAuthManager = clientAuthManager;
        this.workerAuthManager = workerAuthManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    }

    /**
     * Method is responsible for registering a new worker
     *
     * @param dto of type WorkerRegisterDTO
     * @throws DuplicateException when user principal exists
     * @return ResponseEntity of type String
     * */
    public ResponseEntity<?> workerRegister(WorkerRegisterDTO dto) {
        if (this.workerRepo.principalExists(dto.email().trim(), dto.username()) > 0) {
            throw new DuplicateException(dto.email() + " exists");
        }

        var worker = new Worker(
                dto.name().trim(),
                dto.email().trim(),
                dto.username().trim(),
                passwordEncoder.encode(dto.password()),
                true, true, true, true
        );
        worker.addRole(new WorkerRole(WORKER));
        worker.addRole(new WorkerRole(CLIENT));

        this.workerRepo.save(worker);
        return new ResponseEntity<>("Registered", CREATED);
    }

    // TODO validate dto not null, empty etc
    /**
     * Method is responsible for registering a new client
     *
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     * @return ResponseEntity of type String
     * */
    public ResponseEntity<?> clientRegister(ClientRegisterDTO dto) {
        if (this.clientRepo.principalExists(dto.email().trim()) > 0) {
            throw new DuplicateException(dto.email() + " exists");
        }

        var clientz = new Clientz(
                dto.firstname().trim(),
                dto.lastname().trim(),
                dto.email().trim(),
                dto.phone_number().trim(),
                passwordEncoder.encode(dto.password()),
                true, true, true, true
        );
        clientz.addRole(new ClientRole(CLIENT));

        // Save client
        this.clientRepo.save(clientz);
        return new ResponseEntity<>("Registered", CREATED);
    }

    /**
     * Responsible for validating login request made by a user (client or worker).
     * 1. Method validates a user via the appropriate AuthenticationManager
     * 2. Because I am using a custom endpoint, validating max session via SessionAuthenticationStrategy
     * 3. Update SecurityContextHolderStrategy and SecurityContextRepository in-between requests.
     * 4. Encode user role and send as cookie to UI inorder to load the right page for based on user role.
     * Note Transactional annotation is used because Clientz has properties with fetch type LAZY
     *
     * @param key parses the request to know if is a client or worker request
     * @param dto consist of principal(username or email) and password.
     * @param req of type HttpServletRequest
     * @param res of type HttpServletResponse
     * @throws IllegalArgumentException is thrown when wrong key is entered
     * @throws AuthenticationException is thrown when credentials do not exist or bad credentials
     * @return ResponseEntity of type AuthResponse and HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> login(String key, LoginDTO dto, HttpServletRequest req, HttpServletResponse res) {
        switch (key) {
            case "client" -> {
                return loginLogic(this.clientAuthManager, dto, req, res);
            }
            case "worker" -> {
                return loginLogic(this.workerAuthManager, dto, req, res);
            }
            default -> throw new IllegalArgumentException("Invalid key");
        }
    }

    private ResponseEntity<?> loginLogic(
            AuthenticationManager manager,
            LoginDTO dto,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        Authentication authentication = manager
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.principal(), dto.password()));

        // Validate max session
        this.sessionAuthenticationStrategy.onAuthentication(authentication, req, res);

        // Create a new context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        // Update SecurityContextHolder and Strategy
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, req, res);

        // Build response
        String list = authentication.getAuthorities().stream().map(String::valueOf).toList().toString();

        // Set custom cookie to replace using local storage to keep track of isLogged in. Look auth.service.ts
        Cookie cookie = new Cookie(IS_LOGGED_IN, URLEncoder.encode(list, StandardCharsets.UTF_8));
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath(COOKIE_PATH);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(COOKIE_MAX_AGE);

        // Add custom cookie to response
        res.addCookie(cookie);

        return new ResponseEntity<>(new AuthResponse(dto.principal()), OK);
    }

}
