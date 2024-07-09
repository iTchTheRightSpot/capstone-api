package dev.webserver.security.controller;

import dev.webserver.security.CapstoneUserDetails;
import dev.webserver.enumeration.RoleEnum;
import dev.webserver.exception.DuplicateException;
import dev.webserver.external.log.ILogEventPublisher;
import dev.webserver.security.JwtService;
import dev.webserver.user.ClientRole;
import dev.webserver.user.SarreBrandUser;
import dev.webserver.user.UserRepository;
import dev.webserver.user.UserRoleRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static dev.webserver.enumeration.RoleEnum.CLIENT;
import static dev.webserver.enumeration.RoleEnum.WORKER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Value(value = "${server.servlet.session.cookie.name}")
    @Setter @Getter
    private String jsessionid;
    @Value(value = "${server.servlet.session.cookie.secure}")
    @Setter
    private boolean secure;
    @Value(value = "${server.servlet.session.cookie.max-age}")
    @Setter
    private int maxage;
    @Value(value = "${server.servlet.session.cookie.path}")
    private String path;

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService tokenService;
    private final ILogEventPublisher publisher;

    /**
     * Method called by either {@link WorkerAuthController}
     * and {@link ClientAuthController} to register a user.
     *
     * @param res is a {@link HttpServletResponse} where we add a jwt token
     *            based on the controller class that calls it.
     * @param dto is an object of {@link RegisterDto} containing the
     *            necessary details to create a user.
     * @param key is of {@link RoleEnum}. If is equal to CLIENT, we add a
     *            jwt cookie to {@link HttpServletResponse}.
     */
    @Transactional(rollbackOn = Exception.class)
    public void register(HttpServletResponse res, RegisterDto dto, RoleEnum key) {
        if (key.equals(CLIENT)) {
            clientRegister(dto, res);
        } else {
            workerRegister(dto);
        }

        final LocalDateTime utc = LocalDateTime.now(ZoneOffset.UTC);
        final String date = utc.toLocalDate().format(DateTimeFormatter.ofPattern("E dd MMMM uuuu"));
        final String time = utc.toLocalTime().format(DateTimeFormatter.ofPattern("H:m a"));

        final String message = """
                    ## __**%s with role %s**__ registered on %s at %s @everyone
                    """.formatted(dto.firstname(), key.name(), date, time);

        publisher.publishLog(new ConcurrentLinkedQueue<>(List.of(message)));
    }

    /**
     * Responsible for registering a new worker. Logic is throw an error if client
     * has a role of Worker or else add ROLE worker to client.
     *
     * @param dto contains the necessary details to create a {@link SarreBrandUser}.
     * @throws DuplicateException when user principal exists and has a role of worker.
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
     * Registers and automatically signs in a new user.
     *
     * @param dto contains the necessary details to create a {@link SarreBrandUser}.
     * @throws DuplicateException when user principal exists.
     */
    void clientRegister(RegisterDto dto, HttpServletResponse response) {
        if (this.userRepository.userByPrincipal(dto.email().trim()).isPresent()) {
            throw new DuplicateException(dto.email() + " exists");
        }

        var user = createUser(dto);

        var authenticated = UsernamePasswordAuthenticationToken
                .authenticated(user.getEmail(), null, new CapstoneUserDetails(user).getAuthorities());

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
        if (validateCookies(req, res, key)) {
            return;
        }

        var unauthenticated = UsernamePasswordAuthenticationToken
                .unauthenticated(dto.principal().trim(), dto.password());

        var authenticated = this.authManager.authenticate(unauthenticated);

        loginImpl(authenticated, res);

        final LocalDateTime utc = LocalDateTime.now(ZoneOffset.UTC);
        final String date = utc.toLocalDate().format(DateTimeFormatter.ofPattern("E dd MMMM uuuu"));
        final String time = utc.toLocalTime().format(DateTimeFormatter.ofPattern("H:m a"));

        final String message = """
                    ## __**%s with role %s**__ logged in on %s at %s @everyone
                    """.formatted(dto.principal(), key.name(), date, time);

        publisher.publishLog(new ConcurrentLinkedQueue<>(List.of(message)));
    }

    private void loginImpl(Authentication auth, HttpServletResponse response) {
        if (response == null) return;

        String token = tokenService.generateToken(auth);

        Cookie cookie = new Cookie(jsessionid, token);
        cookie.setMaxAge(maxage);
        cookie.setHttpOnly(true);
        cookie.setPath(path);
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
     * @param request of HttpServletRequest.
     * @return boolean true if jwt contains a valid cookie else false.
     */
    private boolean validateCookies(HttpServletRequest request, HttpServletResponse response, RoleEnum role) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;

        Optional<Cookie> first = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(jsessionid) && tokenService.matchesRole(cookie, role))
                .findFirst();

        first.ifPresent(cookie -> {
            cookie.setValue(cookie.getValue());
            cookie.setHttpOnly(true);
            cookie.setMaxAge(cookie.getMaxAge());
            cookie.setPath(path);

            // add cookie to response
            response.addCookie(cookie);
        });

        return first.isPresent();
    }

}