package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.jwt.JwtTokenService;
import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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
    private String COOKIEPATH;

    @Value(value = "${server.servlet.session.cookie.same-site}")
    private String SAMESITE;

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtTokenService = jwtTokenService;
    }

    private record CustomAuthResponse(boolean status, ResponseEntity<?> response) { }

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
     * Basically logs in a user based on credentials stored in the DB.
     * @param dto consist of principal and password.
     * @param request of HttpServletRequest
     * @param response of HttpServletResponse
     * @throws AuthenticationException is thrown when credentials do not exist, bad credentials account is locked e.t.c.
     * @return ResponseEntity
     * */
    @Transactional
    public ResponseEntity<?> login(LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        // No need to re-authenticate if request contains valid jwt cookie
        var customAuthResponse = _validateRequestContainsValidCookies(dto, request);
        if (customAuthResponse.status) {
            return customAuthResponse.response;
        }

        var authentication = this.authManager.authenticate(UsernamePasswordAuthenticationToken
                .unauthenticated(dto.getPrincipal(), dto.getPassword())
        );

        // Jwt Token
        String token = this.jwtTokenService.generateToken(authentication);

        // Servlet cookie because I am setting it in app properties
        // Add jwt to cookie
        Cookie jwtCookie = new Cookie(JSESSIONID, token);
        jwtCookie.setDomain(DOMAIN);
        jwtCookie.setMaxAge(this.jwtTokenService.maxAge());
        jwtCookie.setHttpOnly(HTTPONLY);
        jwtCookie.setPath(COOKIEPATH);
        jwtCookie.setSecure(COOKIESECURE);
        response.addCookie(jwtCookie);

        // org.springframework.http ResponseCookie because I need to set same site
        // Second cookie where UI can access to validate if user is logged in
        ResponseCookie stateCookie = ResponseCookie
                .from(LOGGEDSESSION, URLEncoder.encode(dto.getPrincipal(), StandardCharsets.UTF_8))
                .domain(DOMAIN)
                .maxAge(this.jwtTokenService.maxAge())
                .httpOnly(false)
                .sameSite(SAMESITE)
                .secure(COOKIESECURE)
                .path(COOKIEPATH)
                .build();

        // Add cookie to response header
        HttpHeaders headers = new HttpHeaders();
        headers.add(SET_COOKIE, stateCookie.toString());

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

    /**
     * Method simply prevents user from signing in again if the request contains a valid jwt and LOGGEDSESSION cookie.
     * @param dto of type LoginDTO
     * @param res of HttpServletRequest
     * @return CustomAuthResponse
     * */
    private CustomAuthResponse _validateRequestContainsValidCookies(LoginDTO dto, HttpServletRequest res) {
        Cookie[] cookies = res.getCookies();
        // Base case
        if (cookies == null)
            return new CustomAuthResponse(false, new ResponseEntity<>(OK));

        // Add all cookies names to a set
        Set<String> set = new HashSet<>();
        for (Cookie cookie : cookies) {
            if (!set.isEmpty() && set.contains(JSESSIONID) && set.contains(LOGGEDSESSION)) {
                break;
            }
            set.add(cookie.getName());
        }

        if (set.isEmpty()) {
            return new CustomAuthResponse(false, new ResponseEntity<>(OK));
        }

        boolean bool = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JSESSIONID))
                .anyMatch(this.jwtTokenService::_isTokenNoneExpired);

        if (set.contains(LOGGEDSESSION) && bool) {
            return new CustomAuthResponse(true, new ResponseEntity<>(OK));
        } else if (!set.contains(LOGGEDSESSION) && bool) {
            HttpHeaders headers = new HttpHeaders();
            ResponseCookie cookie = ResponseCookie
                    .from(LOGGEDSESSION, URLEncoder.encode(dto.getPrincipal(), StandardCharsets.UTF_8))
                    .domain(DOMAIN)
                    .maxAge(this.jwtTokenService.maxAge())
                    .httpOnly(false)
                    .sameSite(SAMESITE)
                    .secure(COOKIESECURE)
                    .path(COOKIEPATH)
                    .build();

            // Add cookies to response header
            headers.add(SET_COOKIE, cookie.toString());
            return new CustomAuthResponse(true, ResponseEntity.ok().headers(headers).build());
        }

        return new CustomAuthResponse(false, new ResponseEntity<>(OK));
    }

}
