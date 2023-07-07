package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.clientz.entity.ClientRole;
import com.emmanuel.sarabrandserver.clientz.entity.Clientz;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.jwt.JwtTokenService;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

@Service @Setter
public class AuthService {

    @Value(value = "${custom.cookie.frontend}")
    private String LOGGEDSESSION;

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    @Value(value = "${server.servlet.session.cookie.domain}")
    private String DOMAIN;

    @Value(value = "${server.servlet.session.cookie.http-only}")
    private boolean HTTPONLY;

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
    public ResponseEntity<?> workerRegister(RegisterDTO dto) {
        if (this.clientzRepository.isAdmin(dto.getEmail().trim(), dto.getUsername().trim(), RoleEnum.WORKER) > 0) {
            throw new DuplicateException(dto.getUsername() + " exists");
        }

        Optional<Clientz> client = this.clientzRepository.workerExists(dto.getEmail().trim(), dto.getUsername().trim());

        if (client.isEmpty()) {
            client = Optional.of(createClient(dto));
        }

        // Client to ClientRole has a fetch type of EAGER
        client.get().addRole(new ClientRole(RoleEnum.WORKER));

        // Save client
        this.clientzRepository.save(client.get());
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Method is responsible for registering a new user
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     * @return ResponseEntity of type HttpStatus
     * */
    public ResponseEntity<?> clientRegister(RegisterDTO dto) {
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
     * Basically validate dto to what is saved in the DB using an AuthenticationManager and then send jwt token via
     * cookie to prevent saving to local storage and custom cookie where status http only is set to false so the UI can
     * protect pages.
     * Note Transactional annotation is used because Clientz has properties with fetch type LAZY
     * @param dto consist of principal(username or email) and password.
     * @throws AuthenticationException is thrown when credentials do not exist or bad credentials
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> login(LoginDTO dto) {
        Authentication authentication = this.authManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(dto.getPrincipal(), dto.getPassword())
        );

        // Jwt Token
        String token = this.jwtTokenService.generateToken(authentication);

        // Add jwt to cookie
        ResponseCookie resCookie = ResponseCookie.from(JSESSIONID, token)
                .domain(DOMAIN)
                .maxAge(this.jwtTokenService.maxAge())
                .httpOnly(HTTPONLY)
                .secure(COOKIE_SECURE)
                .path(COOKIE_PATH)
                .build();

        // Second cookie where UI can access to validate if user is logged in
        ResponseCookie resCookie1 = ResponseCookie.from(LOGGEDSESSION, UUID.randomUUID().toString())
                .domain(DOMAIN)
                .maxAge(this.jwtTokenService.maxAge())
                .httpOnly(false)
                .secure(COOKIE_SECURE)
                .path(COOKIE_PATH)
                .build();

        // Add cookies to response header
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, resCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, resCookie1.toString());

        return ResponseEntity.ok().headers(headers).build();
    }

    private Clientz createClient(RegisterDTO dto) {
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
