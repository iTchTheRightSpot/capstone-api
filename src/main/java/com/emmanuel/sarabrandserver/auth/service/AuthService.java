package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.jwt.JwtTokenService;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SarreBrandUser;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;

@Service @Setter
public class AuthService {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

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

    /**
     * Responsible for registering a new worker. Logic is throw an error if client has a role of Worker or else add
     * ROLE worker to client.
     * @param dto of type WorkerRegisterDTO
     * @throws DuplicateException when user principal exists and has a role of worker
     * */
    @Transactional
    public void workerRegister(RegisterDTO dto) {
        var client = this.userRepository
                .workerExists(dto.getEmail().trim())
                .orElse(createUser(dto));

        // Note User and Role tables have a relationship fetch type EAGER
        boolean isAdmin = client.getClientRole() //
                .stream() //
                .anyMatch(role -> role.getRole().equals(RoleEnum.WORKER));

        if (isAdmin) {
            throw new DuplicateException(dto.getUsername() + " exists");
        }

        client.addRole(new ClientRole(RoleEnum.WORKER));

        this.userRepository.save(client);
    }

    /**
     * Method is responsible for registering a new user who isn't a worker
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     * */
    @Transactional
    public void clientRegister(RegisterDTO dto) {
        if (this.userRepository.principalExists(dto.getEmail().trim()) > 0) {
            throw new DuplicateException(dto.getEmail() + " exists");
        }
        this.userRepository.save(createUser(dto));
    }

    /**
     * Manually login a user. Gotcha is jwt token is sent as a http true cookie
     * instead of as an Authorization header or body.
     * @param dto consist of principal and password.
     * @param request of HttpServletRequest
     * @param response of HttpServletResponse
     * @throws AuthenticationException is thrown when credentials do not exist, bad credentials account is locked e.t.c.
     * */
    @Transactional
    public void login(LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        // No need to re-authenticate if request contains valid jwt cookie
        if (_validateRequestContainsValidCookies(request)) {
            return;
        }

        var unauthenticated = UsernamePasswordAuthenticationToken
                .unauthenticated(dto.getPrincipal().trim(), dto.getPassword());

        var authenticated = this.authManager.authenticate(unauthenticated);

        // Jwt Token
        String token = this.jwtTokenService.generateToken(authenticated);

        // Add jwt to cookie
        Cookie cookie = new Cookie(JSESSIONID, token);
        cookie.setMaxAge(this.jwtTokenService.maxAge());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(COOKIESECURE);
        response.addCookie(cookie); // Add cookie to response
    }

    /** Create a new User object */
    private SarreBrandUser createUser(RegisterDTO dto) {
        var client = SarreBrandUser.builder()
                .firstname(dto.getFirstname().trim())
                .lastname(dto.getLastname().trim())
                .email(dto.getEmail().trim())
                .phoneNumber(dto.getPhone().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(RoleEnum.CLIENT));
        return client;
    }

    /**
     * Method simply prevents user from signing in again if the request contains a valid jwt and LOGGEDSESSION cookie.
     * @param res of HttpServletRequest
     * @return boolean
     * */
    private boolean _validateRequestContainsValidCookies(HttpServletRequest res) {
        Cookie[] cookies = res.getCookies();
        // Base case
        if (cookies == null)
            return false;

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JSESSIONID))
                .anyMatch(this.jwtTokenService::_isTokenNoneExpired);
    }

}
