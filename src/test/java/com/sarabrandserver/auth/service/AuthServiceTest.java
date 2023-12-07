package com.sarabrandserver.auth.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.jwt.JwtTokenService;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.user.entity.SarreBrandUser;
import com.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static com.sarabrandserver.enumeration.RoleEnum.CLIENT;
import static com.sarabrandserver.enumeration.RoleEnum.WORKER;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest extends AbstractUnitTest {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        this.authService = new AuthService(
                this.userRepository,
                this.passwordEncoder,
                this.authenticationManager,
                this.jwtTokenService
        );
        this.authService.setJSESSIONID(JSESSIONID);
    }

    @Test
    void register_worker_that_doesnt_exist() {
        // Given
        var dto = new RegisterDTO(
                TestingData.worker().getFirstname(),
                TestingData.worker().getLastname(),
                TestingData.worker().getEmail(),
                "",
                TestingData.worker().getPhoneNumber(),
                TestingData.worker().getPassword()
        );

        // When
        when(this.userRepository.workerExists(anyString())).thenReturn(Optional.empty());

        // Then
        this.authService.workerRegister(dto);
        verify(this.userRepository, times(1))
                .save(any(SarreBrandUser.class));
    }

    @Test
    void register_worker_with_already_existing_role_worker() {
        // Given
        var worker = TestingData.worker();
        var dto = new RegisterDTO(
                worker.getFirstname(),
                worker.getLastname(),
                worker.getEmail(),
                "",
                worker.getPhoneNumber(),
                worker.getPassword()
        );

        // When
        when(this.userRepository.workerExists(anyString())).thenReturn(Optional.of(worker));

        // Then
        assertThrows(DuplicateException.class, () -> this.authService.workerRegister(dto));
    }

    /** Simulates registering an existing Clientz but he or she doesn't have a role of WORKER */
    @Test
    void register_worker_with_role_only_client() {
        // Given
        var client = TestingData.client();
        var dto = new RegisterDTO(
                client.getFirstname(),
                client.getLastname(),
                client.getEmail(),
                "",
                client.getPhoneNumber(),
                client.getPassword()
        );

        // When
        when(this.userRepository.workerExists(anyString())).thenReturn(Optional.of(client));

        // Then
        this.authService.workerRegister(dto);
        verify(this.userRepository, times(1)).save(any(SarreBrandUser.class));
    }

    @Test
    void worker_login() {
        // Given
        var dto = new LoginDTO("", TestingData.worker().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Then
        this.authService.login(WORKER, dto, request, response);
        verify(this.authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void worker_login_non_existing_credentials() {
        // Given
        var dto = new LoginDTO("client@client.com", TestingData.worker().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(WORKER, dto, request, response));
    }

    @Test
    void clientRegister() {
        // Given
        var dto = new RegisterDTO(
                TestingData.client().getFirstname(),
                TestingData.client().getLastname(),
                TestingData.client().getEmail(),
                "",
                TestingData.client().getPhoneNumber(),
                TestingData.client().getPassword()
        );

        var user = SarreBrandUser.builder()
                .clientId(1L)
                .email(dto.email())
                .clientRole(new HashSet<>())
                .build();

        HttpServletResponse res = mock(HttpServletResponse.class);

        // When
        when(this.userRepository.principalExists(anyString())).thenReturn(0);
        when(this.passwordEncoder.encode(anyString())).thenReturn(dto.password());
        when(this.userRepository.save(any(SarreBrandUser.class))).thenReturn(user);

        // Then
        this.authService.clientRegister(dto, res);
        verify(this.userRepository, times(1)).save(any(SarreBrandUser.class));
    }

    @Test
    void register_client_existing_principal() {
        // Given
        var dto = new RegisterDTO(
                TestingData.client().getFirstname(),
                TestingData.client().getLastname(),
                TestingData.client().getEmail(),
                "",
                TestingData.client().getPhoneNumber(),
                TestingData.client().getPassword()
        );

        HttpServletResponse res = mock(HttpServletResponse.class);

        // When
        when(this.userRepository.principalExists(anyString())).thenReturn(1);

        // Then
        assertThrows(DuplicateException.class, () -> this.authService.clientRegister(dto, res));
    }

    @Test
    void client_login() {
        // Given
        var dto = new LoginDTO(TestingData.client().getEmail(), TestingData.client().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Then
        this.authService.login(CLIENT, dto, request, response);
        verify(this.authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void client_login_wrong_credentials() {
        // Given
        var dto = new LoginDTO("worker@worker.com", TestingData.client().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(CLIENT, dto, request, response));
    }

}