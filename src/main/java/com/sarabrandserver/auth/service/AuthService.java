package com.sarabrandserver.auth.service;

import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.jwt.JwtTokenService;
import com.sarabrandserver.enumeration.RoleEnum;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import com.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;

import static com.sarabrandserver.enumeration.RoleEnum.CLIENT;
import static com.sarabrandserver.enumeration.RoleEnum.WORKER;

@Service
@RequiredArgsConstructor
@Setter
public class AuthService {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;
    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int MAXAGE;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenService jwtTokenService;

    /**
     * Responsible for registering a new worker. Logic is throw an error if client
     * has a role of Worker or else add ROLE worker to client.
     *
     * @param dto of type WorkerRegisterDTO
     * @throws DuplicateException when user principal exists and has a role of worker
     */
    @Transactional
    public void workerRegister(RegisterDTO dto) {
        var client = this.userRepository
                .workerExists(dto.email().trim())
                .orElse(createUser(dto));

        // Note User and Role tables have a relationship fetch type EAGER
        boolean isAdmin = client.getClientRole() //
                .stream() //
                .anyMatch(role -> role.getRole().equals(WORKER));

        if (isAdmin) {
            throw new DuplicateException(dto.email() + " exists");
        }

        client.addRole(new ClientRole(WORKER));

        this.userRepository.save(client);
    }

    /**
     * Registers and automatically signs in a new user
     *
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     */
    @Transactional
    public void clientRegister(RegisterDTO dto, HttpServletResponse response) {
        if (this.userRepository.principalExists(dto.email().trim()) > 0) {
            throw new DuplicateException(dto.email() + " exists");
        }

        var user = this.userRepository.save(createUser(dto));

        var authenticated = UsernamePasswordAuthenticationToken
                .authenticated(user.getEmail(), null, user.getAuthorities());

        loginImpl(authenticated, response);
    }

    /**
     * Manually login a user. Jwt is sent as a cookie
     * instead of authorization header.
     * Look in application properties for cookie config.
     *
     * @param key performs login details based on controller
     * @param dto      consist of principal and password.
     * @param req  of HttpServletRequest
     * @param res of HttpServletResponse
     * @throws AuthenticationException is thrown when credentials do not exist,
     *                          bad credentials account is locked e.t.c.
     */
    @Transactional
    public void login(
            RoleEnum key,
            LoginDTO dto,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        // No need to re-authenticate if request contains valid jwt cookie
        if (_validateCookies(req, key)) {
            return;
        }

        var unauthenticated = UsernamePasswordAuthenticationToken
                .unauthenticated(dto.principal().trim(), dto.password());

        var authenticated = this.authManager.authenticate(unauthenticated);

        loginImpl(authenticated, res);
    }

    private void loginImpl(Authentication auth, HttpServletResponse response) {
        if (response == null) return;

        String token = this.jwtTokenService.generateToken(auth);

        // Jwt cookie
        Cookie cookie = new Cookie(JSESSIONID, token);
        cookie.setMaxAge(MAXAGE);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(COOKIESECURE);

        // Add token to response
        response.addCookie(cookie);
    }

    /**
     * Create a new User object
     */
    private SarreBrandUser createUser(RegisterDTO dto) {
        var client = SarreBrandUser.builder()
                .firstname(dto.firstname().trim())
                .lastname(dto.lastname().trim())
                .email(dto.email().trim())
                .phoneNumber(dto.phone().trim())
                .password(passwordEncoder.encode(dto.password()))
                .enabled(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(CLIENT));
        return client;
    }

    /**
     * Validates if requests contains a valid jwt cookie.
     * Prevents a generating unnecessary jwt.
     *
     * @param res of HttpServletRequest
     * @return boolean
     */
    private boolean _validateCookies(HttpServletRequest res, RoleEnum role) {
        Cookie[] cookies = res.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JSESSIONID))
                .anyMatch(cookie -> this.jwtTokenService.matchesRole(cookie, role));
    }

}
