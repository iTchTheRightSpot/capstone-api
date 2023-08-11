package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import com.github.javafaker.Faker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
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

    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private SecurityContextRepository securityContextRepository;
    @Mock private ConcurrentSessionControlAuthenticationStrategy strategy;

    @BeforeEach
    void setUp() {
        this.authService = new AuthService(
                this.userRepository,
                this.passwordEncoder,
                this.authenticationManager,
                this.securityContextRepository,
                this.strategy
        );
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
        when(this.userRepository.workerExists(anyString(), anyString())).thenReturn(Optional.empty());

        // Then
        assertEquals(CREATED, this.authService.workerRegister(dto).getStatusCode());
        verify(this.userRepository, times(1)).save(any(SaraBrandUser.class));
    }

    @Test
    void register_worker_with_already_existing_role_worker() {
        // Given
        var worker = worker();
        var dto = new RegisterDTO(
                worker.getFirstname(),
                worker.getLastname(),
                worker.getEmail(),
                worker.getUsername(),
                worker.getPhoneNumber(),
                worker.getPassword()
        );

        // When
        when(this.userRepository.workerExists(anyString(), anyString())).thenReturn(Optional.of(worker));

        // Then
        assertThrows(DuplicateException.class, () -> this.authService.workerRegister(dto));
    }

    /** Simulates registering an existing Clientz but he or she doesn't have a role of WORKER */
    @Test
    void register_worker_with_role_only_client() {
        // Given
        var client = client();
        var dto = new RegisterDTO(
                client.getFirstname(),
                client.getLastname(),
                client.getEmail(),
                client.getUsername(),
                client.getPhoneNumber(),
                client.getPassword()
        );

        // When
        when(this.userRepository.workerExists(anyString(), anyString())).thenReturn(Optional.of(client));

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