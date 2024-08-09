package dev.webserver.security.controller;

import dev.webserver.AbstractUnitTest;
import dev.webserver.data.TestData;
import dev.webserver.exception.DuplicateException;
import dev.webserver.external.log.ILogEventPublisher;
import dev.webserver.security.JwtService;
import dev.webserver.user.ClientRole;
import dev.webserver.user.SarreBrandUser;
import dev.webserver.user.UserRepository;
import dev.webserver.user.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static dev.webserver.enumeration.RoleEnum.CLIENT;
import static dev.webserver.enumeration.RoleEnum.WORKER;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest extends AbstractUnitTest {

    private AuthenticationService authenticationService;

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager manager;
    @Mock private JwtService tokenService;
    @Mock private ILogEventPublisher publisher;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                roleRepository,
                passwordEncoder,
                manager,
                tokenService,
                publisher
        );

        authenticationService.setJsessionid("JSESSIONID");
    }

    @Test
    void registerWorkerThatDoesntExist() {
        // Given
        var dto = new RegisterDto(
                TestData.worker().firstname(),
                TestData.worker().lastname(),
                TestData.worker().email(),
                "",
                TestData.worker().phoneNumber(),
                TestData.worker().password()
        );

        // When
        when(userRepository.userByPrincipal(anyString()))
                .thenReturn(Optional.empty());

        // Then
        authenticationService.workerRegister(dto);

        verify(userRepository, times(1)).save(any(SarreBrandUser.class));
        verify(roleRepository, times(2)).save(any(ClientRole.class));
    }

    @Test
    void registerWorkerWithAlreadyExistingRoleWorker() {
        // Given
        var worker = TestData.worker();
        var dto = new RegisterDto(
                worker.firstname(),
                worker.lastname(),
                worker.email(),
                "",
                worker.phoneNumber(),
                worker.password()
        );

        // When
        when(userRepository.userByPrincipal(anyString()))
                .thenReturn(Optional.of(worker));

        // Then
        assertThrows(DuplicateException.class, () -> authenticationService.workerRegister(dto));
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
                client.firstname(),
                client.lastname(),
                client.email(),
                "",
                client.phoneNumber(),
                client.password()
        );

        // When
        when(userRepository.userByPrincipal(anyString())).thenReturn(Optional.of(client));

        // Then
        authenticationService.workerRegister(dto);
        verify(userRepository, times(0)).save(any(SarreBrandUser.class));
        verify(roleRepository, times(1)).save(any(ClientRole.class));
    }

    @Test
    void workerLogin() {
        // Given
        var dto = new LoginDto(TestData.worker().email(), TestData.worker().password());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // When
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // Then
        authenticationService.login(WORKER, dto, request, response);
        verify(manager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void workerLoginNonExistingCredentials() {
        // Given
        var dto = new LoginDto("client@client.com", TestData.worker().password());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> authenticationService.login(WORKER, dto, request, response));
    }

    @Test
    void clientRegister() {
        // Given
        var dto = new RegisterDto(
                TestData.client().firstname(),
                TestData.client().lastname(),
                TestData.client().email(),
                "",
                TestData.client().phoneNumber(),
                TestData.client().password()
        );

        var user = SarreBrandUser.builder().clientId(1L).email(dto.email()).build();

        HttpServletResponse res = mock(HttpServletResponse.class);

        // When
        when(userRepository.userByPrincipal(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(dto.password());
        when(userRepository.save(any(SarreBrandUser.class))).thenReturn(user);

        // Then
        authenticationService.clientRegister(dto, res);
        verify(userRepository, times(1)).save(any(SarreBrandUser.class));
        verify(roleRepository, times(1)).save(any(ClientRole.class));
    }

    @Test
    void registerClientExistingPrincipal() {
        // Given
        var dto = new RegisterDto(
                TestData.client().firstname(),
                TestData.client().lastname(),
                TestData.client().email(),
                "",
                TestData.client().phoneNumber(),
                TestData.client().password()
        );

        HttpServletResponse res = mock(HttpServletResponse.class);

        // When
        when(userRepository.userByPrincipal(anyString()))
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
        assertThrows(DuplicateException.class, () -> authenticationService.clientRegister(dto, res));
    }

    @Test
    void clientLogin() {
        // Given
        var dto = new LoginDto(TestData.client().email(), TestData.client().password());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // When
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // Then
        authenticationService.login(CLIENT, dto, request, response);
        verify(manager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void clientLoginWrongCredentials() {
        // Given
        var dto = new LoginDto("worker@worker.com", TestData.client().password());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> authenticationService.login(CLIENT, dto, request, response));
    }

}