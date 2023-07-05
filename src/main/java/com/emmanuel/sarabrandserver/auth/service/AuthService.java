package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.clientz.dto.ClientRegisterDTO;
import com.emmanuel.sarabrandserver.clientz.entity.ClientRole;
import com.emmanuel.sarabrandserver.clientz.entity.Clientz;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.jwt.JwtTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Service @Setter
public class AuthService {

    @Value(value = "${custom.cookie.frontend}")
    private String LOGGEDSESSION;

    @Value(value = "${server.servlet.session.cookie.name}")
    private String COOKIENAME;

    @Value(value = "${server.servlet.session.cookie.domain}")
    private String DOMAIN;

    @Value(value = "${server.servlet.session.cookie.http-only}")
    private boolean HTTPONLY;

    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int COOKIEMAXAGE;

    @Value(value = "${server.servlet.session.cookie.path}")
    private String COOKIE_PATH;

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIE_SECURE;

    private final ClientzRepository clientzRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            ClientzRepository clientzRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            JwtTokenService jwtTokenService
    ) {
        this.clientzRepository = clientzRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtTokenService = jwtTokenService;
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

        // Jwt Token
        String token = this.jwtTokenService.generateToken(authentication);

        // Add Jwt Cookie to Header
        Cookie jwtCookie = new Cookie(COOKIENAME, token);
        jwtCookie.setDomain(DOMAIN);
        jwtCookie.setPath(COOKIE_PATH);
        jwtCookie.setSecure(COOKIE_SECURE);
        jwtCookie.setHttpOnly(HTTPONLY);
        jwtCookie.setMaxAge(COOKIEMAXAGE);

        // Add custom cookie to response
        res.addCookie(jwtCookie);

        // Second cookie where UI can access to validate if user is logged in
        Cookie cookie = new Cookie(LOGGEDSESSION, UUID.randomUUID().toString());
        cookie.setDomain(DOMAIN);
        cookie.setPath(COOKIE_PATH);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(COOKIEMAXAGE);

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
