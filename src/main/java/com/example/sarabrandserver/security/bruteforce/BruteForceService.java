package com.example.sarabrandserver.security.bruteforce;

import com.example.sarabrandserver.clientz.repository.ClientzRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

@Service @Setter @Slf4j
public class BruteForceService {
    private int MAX = 5;
    private final BruteForceRepo bruteForceRepo;
    private final ClientzRepository clientzRepository;

    public BruteForceService(BruteForceRepo bruteForceRepo, ClientzRepository clientzRepository) {
        this.bruteForceRepo = bruteForceRepo;
        this.clientzRepository = clientzRepository;
    }

    /**
     * Method to register every login failure of type BadCredentials for a Clientz.
     * @param auth of type Spring Core Authentication
     */
    @Transactional
    public void registerLoginFailure(Authentication auth) {
        log.info("MAX BEFORE BEING BLOCKED {}", this.MAX);
        this.clientzRepository.findByPrincipal(auth.getName()).ifPresent(clientz -> {
            if (!clientz.isAccountNoneLocked()) {
                return;
            }
            log.info("Client is none locked?");

            // Using redis for caching and concurrency
            AtomicBoolean bool = new AtomicBoolean(true);
            this.bruteForceRepo.findByPrincipal(auth.getName()).ifPresent(entity -> {
                bool.set(false);
                if (entity.getFailedAttempt() < this.MAX) {
                    entity.setFailedAttempt(entity.getFailedAttempt() + 1);
                    this.bruteForceRepo.update(entity);
                    return;
                }
                this.clientzRepository.lockClientAccount(false, clientz.getClientId());
                // TODO send client email to change password
            });

            if (bool.get()) {
                this.bruteForceRepo.save(new BruteForceEntity(0, auth.getName()));
            }
        });
    }


    /**
     * Method to delete the BruteForceEntity after a successful login.
     * @param auth of type Spring Core Authentication
     */
    public void resetBruteForceCounter(Authentication auth) {
        this.clientzRepository.findByPrincipal(auth.getName()) //
                .flatMap(clientz -> this.bruteForceRepo.findByPrincipal(auth.getName())) //
                .ifPresent(entity -> this.bruteForceRepo.delete(entity.getPrincipal()));
    }

}
