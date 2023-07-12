package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.clientz.entity.ClientRole;
import com.emmanuel.sarabrandserver.clientz.entity.Clientz;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.jwt.JwtTokenService;
import com.github.javafaker.Faker;
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

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;

    private AuthService authService;

    @Mock private ClientzRepository clientzRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        this.authService = new AuthService(
                this.clientzRepository,
                this.passwordEncoder,
                this.authenticationManager,
                this.jwtTokenService
        );
        this.authService.setJSESSIONID(JSESSIONID);
        this.authService.setDOMAIN(COOKIEDOMAIN);
        this.authService.setCOOKIEPATH(COOKIEPATH);
        this.authService.setCOOKIESECURE(COOKIESECURE);
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
        when(this.clientzRepository.isAdmin(anyString(), anyString(), any(RoleEnum.class))).thenReturn(0);
        when(this.clientzRepository.workerExists(anyString(), anyString())).thenReturn(Optional.empty());

        // Then
        assertEquals(CREATED, this.authService.workerRegister(dto).getStatusCode());
        verify(this.clientzRepository, times(1)).save(any(Clientz.class));
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
        doReturn(1).when(this.clientzRepository).isAdmin(anyString(), anyString(), any(RoleEnum.class));

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
        when(this.clientzRepository.isAdmin(anyString(), anyString(), any(RoleEnum.class))).thenReturn(0);
        doReturn(Optional.of(client())).when(this.clientzRepository).workerExists(anyString(), anyString());

        // Then
        assertEquals(CREATED, this.authService.workerRegister(dto).getStatusCode());
        verify(this.clientzRepository, times(1)).save(any(Clientz.class));
    }

    @Test
    void worker_login() {
        // Given
        var dto = new LoginDTO(worker().getUsername(), worker().getPassword());
        Authentication authentication = Mockito.mock(Authentication.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(this.jwtTokenService.generateToken(any(Authentication.class))).thenReturn("token");

        // Then
        assertEquals(this.authService.login(dto).getStatusCode(), OK);
        verify(this.authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void worker_login_non_existing_credentials() {
        // Given
        var dto = new LoginDTO("client@client.com", worker().getPassword());

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(dto));
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
        when(this.clientzRepository.principalExists(anyString(), anyString())).thenReturn(0);
        when(this.passwordEncoder.encode(anyString())).thenReturn(dto.getPassword());

        // Then
        assertEquals(CREATED, this.authService.clientRegister(dto).getStatusCode());
        verify(this.clientzRepository, times(1)).save(any(Clientz.class));
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
        when(this.clientzRepository.principalExists(anyString(), anyString())).thenReturn(1);

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
        assertEquals(OK, this.authService.login(dto).getStatusCode());
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
        assertThrows(BadCredentialsException.class, () -> this.authService.login(dto));
    }

    private Clientz client() {
        var client = Clientz.builder()
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

    private Clientz worker() {
        var client = Clientz.builder()
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