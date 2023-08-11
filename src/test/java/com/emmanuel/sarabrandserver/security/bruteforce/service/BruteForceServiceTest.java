package com.emmanuel.sarabrandserver.security.bruteforce.service;

import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.auth.bruteforce.BruteForceEntity;
import com.emmanuel.sarabrandserver.auth.bruteforce.BruteForceRepo;
import com.emmanuel.sarabrandserver.auth.bruteforce.BruteForceService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class BruteForceServiceTest {
    private final int MAX = 2;

    private BruteForceService bruteForceService;

    @Mock private BruteForceRepo bruteForceRepo;
    @Mock private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        this.bruteForceService = new BruteForceService(this.bruteForceRepo, this.userRepository);
        this.bruteForceService.setMAX(MAX);
    }

    @Test
    void registerLoginFailure() {
        // Given
        var client = client();
        var bruteEntity = new BruteForceEntity(0, client.getEmail());
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client.getEmail());
        when(this.userRepository.findByPrincipal(anyString())).thenReturn(Optional.of(client));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.of(bruteEntity));

        // Then
        this.bruteForceService.loginFailure(authentication);
        verify(this.userRepository, times(1)).findByPrincipal(client.getEmail());
        verify(this.bruteForceRepo, times(1)).findByPrincipal(client.getEmail());
    }

    /** Method tests when this.bruteForceRepo.findByPrincipal is empty */
    @Test
    void em() {
        // Given
        var client = client();
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client.getEmail());
        when(this.userRepository.findByPrincipal(anyString())).thenReturn(Optional.of(client));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.empty());

        // Then
        this.bruteForceService.loginFailure(authentication);
        verify(userRepository, times(1)).findByPrincipal(client.getEmail());
        verify(bruteForceRepo, times(1)).save(any(BruteForceEntity.class));
        verify(this.userRepository, times(0)).lockClientAccount(anyBoolean(), anyLong());
        verify(this.bruteForceRepo, times(0)).update(any(BruteForceEntity.class));
    }

    /** Simulates bruteEntity.get().getFailedAttempt() >= than max value */
    @Test
    void tt() {
        // Given
        var client = client();
        var bruteEntity = new BruteForceEntity(this.MAX, client.getEmail());
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client.getEmail());
        when(this.userRepository.findByPrincipal(anyString())).thenReturn(Optional.of(client));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.of(bruteEntity));

        // Then
        this.bruteForceService.loginFailure(authentication);
        verify(this.userRepository, times(1)).findByPrincipal(client.getEmail());
        verify(this.bruteForceRepo, times(1)).findByPrincipal(client.getEmail());
        verify(this.bruteForceRepo, times(0)).save(any(BruteForceEntity.class));
        verify(this.userRepository, times(1)).lockClientAccount(false, client.getClientId());
    }

    @Test
    void resetBruteForceCounter() {
        // Given
        var client = client();
        var bruteEntity = new BruteForceEntity(this.MAX, client.getEmail());
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client.getEmail());
        when(this.userRepository.findByPrincipal(anyString())).thenReturn(Optional.of(client));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.of(bruteEntity));

        // Then
        this.bruteForceService.resetBruteForceCounter(authentication);
        verify(this.bruteForceRepo, times(1)).delete(bruteEntity.getPrincipal());
    }

    private SaraBrandUser client() {
        var client = SaraBrandUser.builder()
                .clientId(1L)
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

}