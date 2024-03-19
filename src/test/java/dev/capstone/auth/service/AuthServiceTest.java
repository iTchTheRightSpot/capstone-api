<<<<<<<< HEAD:webserver/src/test/java/dev/webserver/auth/service/AuthServiceTest.java
package dev.webserver.auth.service;

import dev.webserver.AbstractUnitTest;
import dev.webserver.auth.dto.LoginDto;
import dev.webserver.auth.dto.RegisterDto;
import dev.webserver.auth.jwt.JwtTokenService;
import dev.webserver.data.TestData;
import dev.webserver.exception.DuplicateException;
import dev.webserver.user.entity.ClientRole;
import dev.webserver.user.entity.SarreBrandUser;
import dev.webserver.user.repository.UserRepository;
import dev.webserver.user.repository.UserRoleRepository;
========
package dev.capstone.auth.service;

import dev.capstone.AbstractUnitTest;
import dev.capstone.auth.dto.LoginDto;
import dev.capstone.auth.dto.RegisterDto;
import dev.capstone.auth.jwt.JwtTokenService;
import dev.capstone.data.TestData;
import dev.capstone.exception.DuplicateException;
import dev.capstone.user.entity.ClientRole;
import dev.capstone.user.entity.SarreBrandUser;
import dev.capstone.user.repository.UserRepository;
import dev.capstone.user.repository.UserRoleRepository;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/test/java/dev/capstone/auth/service/AuthServiceTest.java
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

<<<<<<<< HEAD:webserver/src/test/java/dev/webserver/auth/service/AuthServiceTest.java
import static dev.webserver.enumeration.RoleEnum.CLIENT;
import static dev.webserver.enumeration.RoleEnum.WORKER;
========
import static dev.capstone.enumeration.RoleEnum.CLIENT;
import static dev.capstone.enumeration.RoleEnum.WORKER;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/test/java/dev/capstone/auth/service/AuthServiceTest.java
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest extends AbstractUnitTest {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager manager;
    @Mock private JwtTokenService tokenService;

    @BeforeEach
    void setUp() {
        this.authService = new AuthService(
                this.userRepository,
                this.roleRepository,
                this.passwordEncoder,
                this.manager,
                this.tokenService
        );
        this.authService.setJSESSIONID(JSESSIONID);
    }

    @Test
    void registerWorkerThatDoesntExist() {
        // Given
        var dto = new RegisterDto(
                TestData.worker().getFirstname(),
                TestData.worker().getLastname(),
                TestData.worker().getEmail(),
                "",
                TestData.worker().getPhoneNumber(),
                TestData.worker().getPassword()
        );

        // When
        when(this.userRepository.userByPrincipal(anyString()))
                .thenReturn(Optional.empty());

        // Then
        this.authService.workerRegister(dto);

        verify(this.userRepository, times(1))
                .save(any(SarreBrandUser.class));
        verify(this.roleRepository, times(2))
                .save(any(ClientRole.class));
    }

    @Test
    void registerWorkerWithAlreadyExistingRoleWorker() {
        // Given
        var worker = TestData.worker();
        var dto = new RegisterDto(
                worker.getFirstname(),
                worker.getLastname(),
                worker.getEmail(),
                "",
                worker.getPhoneNumber(),
                worker.getPassword()
        );

        // When
        when(this.userRepository.userByPrincipal(anyString()))
                .thenReturn(Optional.of(worker));

        // Then
        assertThrows(DuplicateException.class, () -> this.authService.workerRegister(dto));
    }

    /**
     * Simulates registering an existing Clientz but he or she doesn't
     * have a role of WORKER.
     * */
    @Test
    void registerWorkerWithRoleOnlyClient() {
        // Given
        var client = TestData.client();
        var dto = new RegisterDto(
                client.getFirstname(),
                client.getLastname(),
                client.getEmail(),
                "",
                client.getPhoneNumber(),
                client.getPassword()
        );

        // When
        when(this.userRepository.userByPrincipal(anyString()))
                .thenReturn(Optional.of(client));

        // Then
        this.authService.workerRegister(dto);
        verify(this.userRepository, times(0)).save(any(SarreBrandUser.class));
        verify(this.roleRepository, times(1)).save(any(ClientRole.class));
    }

    @Test
    void workerLogin() {
        // Given
        var dto = new LoginDto(TestData.worker().getEmail(), TestData.worker().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // When
        when(this.manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Then
        this.authService.login(WORKER, dto, request, response);
        verify(this.manager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void workerLoginNonExistingCredentials() {
        // Given
        var dto = new LoginDto("client@client.com", TestData.worker().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        when(this.manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(WORKER, dto, request, response));
    }

    @Test
    void clientRegister() {
        // Given
        var dto = new RegisterDto(
                TestData.client().getFirstname(),
                TestData.client().getLastname(),
                TestData.client().getEmail(),
                "",
                TestData.client().getPhoneNumber(),
                TestData.client().getPassword()
        );

        var user = SarreBrandUser.builder()
                .clientId(1L)
                .email(dto.email())
                .clientRole(new HashSet<>())
                .build();

        HttpServletResponse res = mock(HttpServletResponse.class);

        // When
        when(this.userRepository.userByPrincipal(anyString())).thenReturn(Optional.empty());
        when(this.passwordEncoder.encode(anyString())).thenReturn(dto.password());
        when(this.userRepository.save(any(SarreBrandUser.class))).thenReturn(user);

        // Then
        this.authService.clientRegister(dto, res);
        verify(this.userRepository, times(1))
                .save(any(SarreBrandUser.class));
        verify(this.roleRepository, times(1))
                .save(any(ClientRole.class));
    }

    @Test
    void registerClientExistingPrincipal() {
        // Given
        var dto = new RegisterDto(
                TestData.client().getFirstname(),
                TestData.client().getLastname(),
                TestData.client().getEmail(),
                "",
                TestData.client().getPhoneNumber(),
                TestData.client().getPassword()
        );

        HttpServletResponse res = mock(HttpServletResponse.class);

        // When
        when(this.userRepository.userByPrincipal(anyString()))
                .thenReturn(Optional
                        .of(SarreBrandUser.builder()
                                .firstname(dto.firstname())
                                .lastname(dto.lastname())
                                .email(dto.email())
                                .phoneNumber(dto.phone())
                                .password(dto.password())
                                .build()
                        )
                );

        // Then
        assertThrows(DuplicateException.class, () -> this.authService.clientRegister(dto, res));
    }

    @Test
    void clientLogin() {
        // Given
        var dto = new LoginDto(TestData.client().getEmail(), TestData.client().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // When
        when(this.manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Then
        this.authService.login(CLIENT, dto, request, response);
        verify(this.manager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void clientLoginWrongCredentials() {
        // Given
        var dto = new LoginDto("worker@worker.com", TestData.client().getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        when(this.manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(CLIENT, dto, request, response));
    }

}