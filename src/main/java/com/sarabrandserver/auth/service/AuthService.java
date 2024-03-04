package com.sarabrandserver.auth.service;

import com.sarabrandserver.auth.dto.LoginDto;
import com.sarabrandserver.auth.dto.RegisterDto;
import com.sarabrandserver.auth.jwt.JwtTokenService;
import com.sarabrandserver.enumeration.RoleEnum;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import com.sarabrandserver.user.repository.UserRepository;
import com.sarabrandserver.user.repository.UserRoleRepository;
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
    private boolean secure;
    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int maxage;

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenService tokenService;

    /**
     * Method called by either {@code WorkerAuthController}
     * and {@code ClientAuthController} to register a user.
     *
     * @param res is a {@code HttpServletResponse} where we add a jwt token
     *            based on the controller class that calls it.
     * @param dto is an object of {@code RegisterDto} containing the
     *            necessary details to create a user.
     * @param key is of {@code RoleEnum}. If is equal to CLIENT, we add a
     *            jwt cookie to {@code HttpServletResponse}.
     */
    @Transactional
    public void register(HttpServletResponse res, RegisterDto dto, RoleEnum key) {
        if (key.equals(CLIENT)) {
            clientRegister(dto, res);
        } else {
            workerRegister(dto);
        }
    }

    /**
     * Responsible for registering a new worker. Logic is throw an error if client
     * has a role of Worker or else add ROLE worker to client.
     *
     * @param dto of type WorkerRegisterDTO
     * @throws DuplicateException when user principal exists and has a role of worker
     */
    void workerRegister(RegisterDto dto) {
        var optional = this.userRepository.userByPrincipal(dto.email().trim());

        if (optional.isPresent() && optional.get().getClientRole().stream()
                .anyMatch(role -> role.role().equals(WORKER))
        ) {
            throw new DuplicateException(dto.email() + " exists");
        }

        SarreBrandUser userToSave = optional.orElseGet(() -> createUser(dto));

        this.roleRepository.save(new ClientRole(WORKER, userToSave));
    }

    /**
     * Registers and automatically signs in a new user
     *
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     */
    void clientRegister(RegisterDto dto, HttpServletResponse response) {
        if (this.userRepository.userByPrincipal(dto.email().trim()).isPresent()) {
            throw new DuplicateException(dto.email() + " exists");
        }

        var user = createUser(dto);

        var authenticated = UsernamePasswordAuthenticationToken
                .authenticated(
                        user.getEmail(),
                        null,
                        new UserDetailz(user).getAuthorities()
                );

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
    public void login(
            RoleEnum key,
            LoginDto dto,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
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

        String token = this.tokenService.generateToken(auth);

        Cookie cookie = new Cookie(JSESSIONID, token);
        cookie.setMaxAge(maxage);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(secure);

        // add token to response
        response.addCookie(cookie);
    }

    /**
     * Create and a new User object
     */
    private SarreBrandUser createUser(RegisterDto dto) {
        var user = this.userRepository
                .save(SarreBrandUser.builder()
                        .firstname(dto.firstname().trim())
                        .lastname(dto.lastname().trim())
                        .email(dto.email().trim())
                        .phoneNumber(dto.phone().trim())
                        .password(passwordEncoder.encode(dto.password()))
                        .enabled(true)
                        .clientRole(new HashSet<>())
                        .paymentDetail(new HashSet<>())
                        .build()
                );

        this.roleRepository.save(new ClientRole(CLIENT, user));

        return user;
    }

    /**
     * Validates if requests contains a valid jwt cookie.
     * Prevents generating unnecessary jwt.
     *
     * @param res of HttpServletRequest.
     * @return boolean true if jwt contains a valid cookie else false.
     */
    private boolean _validateCookies(HttpServletRequest res, RoleEnum role) {
        Cookie[] cookies = res.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JSESSIONID))
                .anyMatch(cookie -> this.tokenService.matchesRole(cookie, role));
    }

}
