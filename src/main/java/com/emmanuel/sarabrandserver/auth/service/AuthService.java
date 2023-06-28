package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.clientz.dto.ClientRegisterDTO;
import com.emmanuel.sarabrandserver.clientz.entity.ClientRole;
import com.emmanuel.sarabrandserver.clientz.entity.Clientz;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

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

    private final ClientzRepository clientzRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authManager;

    private final SecurityContextRepository securityContextRepository;

    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public AuthService(
            ClientzRepository clientzRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            SecurityContextRepository securityContextRepository,
            @Qualifier(value = "customStrategy") SessionAuthenticationStrategy sessionAuthenticationStrategy
    ) {
        this.clientzRepository = clientzRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    }

    /**
     * Responsible for registering a new worker. The logic is basically update Clientz to a role of worker if
     * he/she exists else create and save new Clientz object.
     * @param dto of type WorkerRegisterDTO
     * @throws DuplicateException when user principal exists and has a role of worker
     * @return ResponseEntity of type String
     * */
    public ResponseEntity<?> workerRegister(ClientRegisterDTO dto) {
        if (this.clientzRepository.isAdmin(dto.getEmail().trim(), dto.getUsername().trim(), RoleEnum.WORKER) > 0) {
            throw new DuplicateException(dto.getUsername() + " exists");
        }

        Optional<Clientz> client = this.clientzRepository.workerExists(dto.getEmail().trim(), dto.getUsername().trim());

        if (client.isPresent()) { // Client to ClientRole has a fetch type of EAGER
            client.get().addRole(new ClientRole(RoleEnum.WORKER));
        } else {
            // Create Client and add role of worker
            client = Optional.of(createClient(dto));
            client.get().addRole(new ClientRole(RoleEnum.WORKER));
        }

        // Save client
        this.clientzRepository.save(client.get());
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Method is responsible for registering a new user
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     * @return ResponseEntity of type String
     * */
    public ResponseEntity<?> clientRegister(ClientRegisterDTO dto) {
        if (this.clientzRepository.principalExists(dto.getEmail().trim(), dto.getUsername().trim()) > 0) {
            throw new DuplicateException(dto.getEmail() + " exists");
        }

        // Create clientz
        var clientz = createClient(dto);

        // Create and Save client
        this.clientzRepository.save(clientz);
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Responsible for validating login request made by a user (client or worker).
     * 1. Method validates a user via the appropriate AuthenticationManager
     * 2. Because I am using a custom endpoint, validating max session via SessionAuthenticationStrategy
     * 3. Update SecurityContextHolderStrategy and SecurityContextRepository in-between requests.
     * 4. Encode user role and send as cookie to UI inorder to load the right page for based on user role.
     * Note Transactional annotation is used because Clientz has properties with fetch type LAZY
     * @param dto consist of principal(username or email) and password.
     * @param req of type HttpServletRequest
     * @param res of type HttpServletResponse
     * @throws AuthenticationException is thrown when credentials do not exist or bad credentials
     * @return ResponseEntity of type AuthResponse and HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> login(LoginDTO dto, HttpServletRequest req, HttpServletResponse res) {
        Authentication authentication = this.authManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(dto.getPrincipal(), dto.getPassword())
        );

        // Validate max session
        this.sessionAuthenticationStrategy.onAuthentication(authentication, req, res);

        // Create a new context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        // Update SecurityContextHolder and Strategy
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, req, res);

        // Build response
        // Set custom cookie to replace using local storage to keep track of isLogged in. Look auth.service.ts
        Cookie cookie = new Cookie(IS_LOGGED_IN, UUID.randomUUID().toString());
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath(COOKIE_PATH);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(COOKIE_MAX_AGE);

        // Add custom cookie to response
        res.addCookie(cookie);

        return new ResponseEntity<>(OK);
    }

    private Clientz createClient(ClientRegisterDTO dto) {
        var client = Clientz.builder()
                .firstname(dto.getFirstname().trim())
                .lastname(dto.getLastname().trim())
                .email(dto.getEmail().trim())
                .username(dto.getUsername().trim())
                .phoneNumber(dto.getPhone_number().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(true)
                .credentialsNonExpired(true)
                .accountNonExpired(true)
                .accountNoneLocked(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(RoleEnum.CLIENT));
        return client;
    }

}
