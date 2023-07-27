package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import java.util.HashSet;

import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.CREATED;

@Service @Setter
public class AuthService {

    @Value(value = "${custom.cookie.frontend}")
    private String LOGGEDSESSION;

    @Value(value = "${server.servlet.session.cookie.domain}")
    private String DOMAIN;

    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int MAXAGE;

    @Value(value = "${server.servlet.session.cookie.path}")
    private String PATH;

    @Value(value = "${server.servlet.session.cookie.same-site}")
    private String SAMESITE;

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean SECURE;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy;
    private final SessionAuthenticationStrategy strategy;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            SecurityContextRepository securityContextRepository,
            @Qualifier(value = "strategy") SessionAuthenticationStrategy strategy
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.securityContextRepository = securityContextRepository;
        this.strategy = strategy;
        // Info about securitycontextholder https://stackoverflow.com/questions/74458719/isnt-securitycontextholder-a-bean
        this.securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    }

    /**
     * Responsible for registering a new worker. The logic is basically update Clientz to a role of worker if
     * he/she exists else create and save new Clientz object.
     * @param dto of type WorkerRegisterDTO
     * @throws DuplicateException when user principal exists and has a role of worker
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> workerRegister(RegisterDTO dto) {
        boolean bool = this.userRepository
                .isAdmin(dto.getEmail().trim(), dto.getUsername().trim(), RoleEnum.WORKER) > 0;
        if (bool) {
            throw new DuplicateException(dto.getUsername() + " exists");
        }

        SaraBrandUser client = this.userRepository
                .workerExists(dto.getEmail().trim(), dto.getUsername().trim())
                .orElse(createUser(dto));
        client.addRole(new ClientRole(RoleEnum.WORKER));

        this.userRepository.save(client);
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Method is responsible for registering a new user who isn't a worker
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> clientRegister(RegisterDTO dto) {
        if (this.userRepository.principalExists(dto.getEmail().trim(), dto.getUsername().trim()) > 0) {
            throw new DuplicateException(dto.getEmail() + " exists");
        }
        this.userRepository.save(createUser(dto));
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Basically logs in a user based on credentials stored in the DB. The only gotcha is we are sending a custom cookie
     * which the ui needs to protect pages.
     * @param dto consist of principal and password.
     * @param request of HttpServletRequest
     * @param response of HttpServletResponse
     * @throws AuthenticationException is thrown when credentials do not exist, bad credentials account is locked e.t.c.
     * @return ResponseEntity
     * */
    @Transactional
    public ResponseEntity<?> login(LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        var authenticated = this.authManager
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.getPrincipal(), dto.getPassword()));

        // Create a new context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);

        // Strategy
        this.strategy.onAuthentication(authenticated, request, response);

        // Update SecurityContextHolder and Strategy
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        // Custom cookie
        ResponseCookie cookie = ResponseCookie
                .from(LOGGEDSESSION, URLEncoder.encode(dto.getPrincipal(), StandardCharsets.UTF_8))
                .domain(DOMAIN)
                .httpOnly(false)
                .secure(SECURE)
                .path(PATH)
                .maxAge(MAXAGE)
                .sameSite(SAMESITE)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().headers(headers).build();
    }

    /** Create a new Clientz object */
    private SaraBrandUser createUser(RegisterDTO dto) {
        var client = SaraBrandUser.builder()
                .firstname(dto.getFirstname().trim())
                .lastname(dto.getLastname().trim())
                .email(dto.getEmail().trim())
                .username(dto.getUsername().trim())
                .phoneNumber(dto.getPhone().trim())
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
