package com.example.sarabrandserver.security.bruteforce.service;

import com.example.sarabrandserver.enumeration.RoleEnum;
import com.example.sarabrandserver.security.bruteforce.entity.BruteForceEntity;
import com.example.sarabrandserver.security.bruteforce.repository.BruteForceRepo;
import com.example.sarabrandserver.user.entity.ClientRole;
import com.example.sarabrandserver.user.entity.Clientz;
import com.example.sarabrandserver.user.repository.ClientRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class BruteForceServiceTest {
    private final int MAX = 2;
    private BruteForceService bruteForceService;
    @Mock private BruteForceRepo bruteForceRepo;
    @Mock private ClientRepo clientRepo;

    @BeforeEach
    void setUp() {
        this.bruteForceService = new BruteForceService(this.bruteForceRepo, this.clientRepo);
        this.bruteForceService.setMAX(MAX);
    }

    @Test
    void registerLoginFailure() {
        // Given
        var bruteEntity = new BruteForceEntity(0, client().getEmail());
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client().getEmail());
        when(this.clientRepo.findByPrincipal(anyString())).thenReturn(Optional.of(client()));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.of(bruteEntity));

        // Then
        this.bruteForceService.registerLoginFailure(authentication);
        verify(clientRepo, times(1)).findByPrincipal(client().getEmail());
        verify(bruteForceRepo, times(1)).findByPrincipal(client().getEmail());
    }

    /** Method tests when this.bruteForceRepo.findByPrincipal is empty */
    @Test
    void em() {
        // Given
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client().getEmail());
        when(this.clientRepo.findByPrincipal(anyString())).thenReturn(Optional.of(client()));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.empty());

        // Then
        this.bruteForceService.registerLoginFailure(authentication);
        verify(clientRepo, times(1)).findByPrincipal(client().getEmail());
        verify(bruteForceRepo, times(1)).save(any(BruteForceEntity.class));
        verify(this.clientRepo, times(0)).lockClientz(anyBoolean(), anyLong());
        verify(this.bruteForceRepo, times(0)).update(any(BruteForceEntity.class));
    }

    /** Simulates bruteEntity.get().getFailedAttempt() >= than max value */
    @Test
    void tt() {
        // Given
        var bruteEntity = new BruteForceEntity(this.MAX, client().getEmail());
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client().getEmail());
        when(this.clientRepo.findByPrincipal(anyString())).thenReturn(Optional.of(client()));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.of(bruteEntity));

        // Then
        this.bruteForceService.registerLoginFailure(authentication);
        verify(this.clientRepo, times(1)).findByPrincipal(client().getEmail());
        verify(this.bruteForceRepo, times(1)).findByPrincipal(client().getEmail());
        verify(this.bruteForceRepo, times(0)).save(any(BruteForceEntity.class));
        verify(this.clientRepo, times(1)).lockClientz(false, client().getClientId());
    }

    @Test
    void resetBruteForceCounter() {
        // Given
        var bruteEntity = new BruteForceEntity(this.MAX, client().getEmail());
        var authentication = mock(Authentication.class);

        // When
        when(authentication.getName()).thenReturn(client().getEmail());
        when(this.clientRepo.findByPrincipal(anyString())).thenReturn(Optional.of(client()));
        when(this.bruteForceRepo.findByPrincipal(anyString())).thenReturn(Optional.of(bruteEntity));

        // Then
        this.bruteForceService.resetBruteForceCounter(authentication);
        verify(this.bruteForceRepo, times(1)).delete(bruteEntity.getPrincipal());
    }

    private Clientz client() {
        var user = new Clientz();
        user.setClientId(1L);
        user.setFirstname("SEJU");
        user.setLastname("development");
        user.setEmail("admin@admin.com");
        user.setUsername("Admin Development");
        user.setPhoneNumber("000-000-0000");
        user.setPassword("password");
        user.setEnabled(true);
        user.setLocked(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonExpired(true);
        user.addRole(new ClientRole(RoleEnum.CLIENT));
        return user;
    }

}