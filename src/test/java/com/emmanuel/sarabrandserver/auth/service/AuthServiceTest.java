package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.jwt.JwtTokenService;
import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import com.github.javafaker.Faker;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthServiceTest {

    @Value(value = "${custom.cookie.frontend}")
    private String LOGGEDSESSION;

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    @Value(value = "${server.servlet.session.cookie.domain}")
    private String COOKIEDOMAIN;

    @Value(value = "${server.servlet.session.cookie.path}")
    private String COOKIEPATH;

    @Value(value = "${server.servlet.session.cookie.same-site}")
    private String SAMESITE;

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;

    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private CustomUtil customUtil;

    @BeforeEach
    void setUp() {
        this.authService = new AuthService(
                this.userRepository,
                this.passwordEncoder,
                this.authenticationManager,
                this.jwtTokenService
        );
        this.authService.setJSESSIONID(JSESSIONID);
        this.authService.setDOMAIN(COOKIEDOMAIN);
        this.authService.setCOOKIEPATH(COOKIEPATH);
        this.authService.setCOOKIESECURE(COOKIESECURE);
        this.authService.setSAMESITE(SAMESITE);
        this.authService.setLOGGEDSESSION(LOGGEDSESSION);
    }

    @Test
    void register_worker_that_doesnt_exist() {
        // Given
        var dto = new RegisterDTO(
                worker().getFirstname(),
                worker().getLastname(),
                worker().getEmail(),
                worker().getUsername(),
                worker().getPhoneNumber(),
                worker().getPassword()
        );

        // When
        when(this.userRepository.isAdmin(anyString(), anyString(), any(RoleEnum.class))).thenReturn(0);
        when(this.userRepository.workerExists(anyString(), anyString())).thenReturn(Optional.empty());

        // Then
        assertEquals(CREATED, this.authService.workerRegister(dto).getStatusCode());
        verify(this.userRepository, times(1)).save(any(SaraBrandUser.class));
    }

    @Test
    void register_worker_with_already_existing_role_worker() {
        // Given
        var dto = new RegisterDTO(
                worker().getFirstname(),
                worker().getLastname(),
                worker().getEmail(),
                worker().getUsername(),
                worker().getPhoneNumber(),
                worker().getPassword()
        );

        // When
        doReturn(1).when(this.userRepository).isAdmin(anyString(), anyString(), any(RoleEnum.class));

        // Then
        assertThrows(DuplicateException.class, () -> this.authService.workerRegister(dto));
    }

    /** Simulates registering an existing Clientz but he or she doesn't have a role of WORKER */
    @Test
    void register_worker_with_role_only_client() {
        // Given
        var dto = new RegisterDTO(
                client().getFirstname(),
                client().getLastname(),
                client().getEmail(),
                client().getUsername(),
                client().getPhoneNumber(),
                client().getPassword()
        );

        // When
        when(this.userRepository.isAdmin(anyString(), anyString(), any(RoleEnum.class))).thenReturn(0);
        doReturn(Optional.of(client())).when(this.userRepository).workerExists(anyString(), anyString());

        // Then
        assertEquals(CREATED, this.authService.workerRegister(dto).getStatusCode());
        verify(this.userRepository, times(1)).save(any(SaraBrandUser.class));
    }

    @Test
    void worker_login() {
        // Given
        var dto = new LoginDTO(worker().getUsername(), worker().getPassword());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(this.jwtTokenService.generateToken(any(Authentication.class))).thenReturn("token");

        // Then
        assertEquals(this.authService.login(dto, request, response).getStatusCode(), OK);
        verify(this.authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void worker_login_non_existing_credentials() {
        // Given
        var dto = new LoginDTO("client@client.com", worker().getPassword());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(dto, request, response));
    }

    /** Simulates jwt and LOGGEDSESSION cookies are present in the request */
    @Test
    void validateLoginRequestWithExistingJwtAndLoggedSessionCookie() {
        // Given
        var dto = new LoginDTO(worker().getUsername(), worker().getPassword());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Cookie cookie1 = new Cookie(JSESSIONID, UUID.randomUUID().toString());
        cookie1.setDomain(COOKIEDOMAIN);
        cookie1.setMaxAge(1800);
        cookie1.setHttpOnly(false);
        cookie1.setPath(COOKIEPATH);
        cookie1.setSecure(COOKIESECURE);

        Cookie cookie2 = new Cookie(LOGGEDSESSION, UUID.randomUUID().toString());
        cookie2.setDomain(COOKIEDOMAIN);
        cookie2.setMaxAge(1800);
        cookie2.setHttpOnly(false);
        cookie2.setPath(COOKIEPATH);
        cookie2.setSecure(COOKIESECURE);

        // When
        when(request.getCookies()).thenReturn(new Cookie[] { cookie1, cookie2 });
        when(this.jwtTokenService._isTokenNoneExpired(any(Cookie.class))).thenReturn(true);

        // Then
        assertEquals(this.authService.login(dto, request, response).getStatusCode(), OK);
        verify(this.authenticationManager, times(0)).authenticate(any(Authentication.class));
    }

    /** Simulates only jwt cookie is present in request */
    @Test
    void validateLoginRequestWithJwtCookieOnly() {
        // Given
        var dto = new LoginDTO(worker().getUsername(), worker().getPassword());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Cookie cookie1 = new Cookie(JSESSIONID, UUID.randomUUID().toString());
        cookie1.setDomain(COOKIEDOMAIN);
        cookie1.setMaxAge(1800);
        cookie1.setHttpOnly(false);
        cookie1.setPath(COOKIEPATH);
        cookie1.setSecure(COOKIESECURE);

        // When
        when(request.getCookies()).thenReturn(new Cookie[] { cookie1 });
        when(this.jwtTokenService._isTokenNoneExpired(any(Cookie.class))).thenReturn(true);

        // Then
        assertEquals(this.authService.login(dto, request, response).getStatusCode(), OK);
        verify(this.authenticationManager, times(0)).authenticate(any(Authentication.class));
    }

    @Test
    void clientRegister() {
        // Given
        var dto = new RegisterDTO(
                client().getFirstname(),
                client().getLastname(),
                client().getEmail(),
                client().getUsername(),
                client().getPhoneNumber(),
                client().getPassword()
        );

        // When
        when(this.userRepository.principalExists(anyString(), anyString())).thenReturn(0);
        when(this.passwordEncoder.encode(anyString())).thenReturn(dto.getPassword());

        // Then
        assertEquals(CREATED, this.authService.clientRegister(dto).getStatusCode());
        verify(this.userRepository, times(1)).save(any(SaraBrandUser.class));
    }

    @Test
    void register_client_existing_principal() {
        // Given
        var dto = new RegisterDTO(
                client().getFirstname(),
                client().getLastname(),
                client().getEmail(),
                client().getUsername(),
                client().getPhoneNumber(),
                client().getPassword()
        );

        // When
        when(this.userRepository.principalExists(anyString(), anyString())).thenReturn(1);

        // Then
        assertThrows(DuplicateException.class, () -> this.authService.clientRegister(dto));
    }

    @Test
    void client_login() {
        // Given
        var dto = new LoginDTO(client().getEmail(), client().getPassword());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(this.jwtTokenService.generateToken(any(Authentication.class))).thenReturn("token");

        // Then
        assertEquals(OK, this.authService.login(dto, request, response).getStatusCode());
        verify(this.authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void client_login_wrong_credentials() {
        // Given
        var dto = new LoginDTO("worker@worker.com", client().getPassword());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(dto, request, response));
    }

    private SaraBrandUser client() {
        var client = SaraBrandUser.builder()
                .firstname(new Faker().name().firstName())
                .lastname(new Faker().name().lastName())
                .email(new Faker().name().fullName())
                .username(new Faker().name().username())
                .phoneNumber(new Faker().phoneNumber().phoneNumber())
                .password(new Faker().phoneNumber().phoneNumber())
                .enabled(true)
                .credentialsNonExpired(true)
                .accountNonExpired(true)
                .accountNoneLocked(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(RoleEnum.CLIENT));
        return client;
    }

    private SaraBrandUser worker() {
        var client = SaraBrandUser.builder()
                .firstname(new Faker().name().firstName())
                .lastname(new Faker().name().lastName())
                .email(new Faker().name().fullName())
                .username(new Faker().name().username())
                .phoneNumber(new Faker().phoneNumber().phoneNumber())
                .password(new Faker().phoneNumber().phoneNumber())
                .enabled(true)
                .credentialsNonExpired(true)
                .accountNonExpired(true)
                .accountNoneLocked(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(RoleEnum.CLIENT));
        client.addRole(new ClientRole(RoleEnum.WORKER));
        return client;
    }

}