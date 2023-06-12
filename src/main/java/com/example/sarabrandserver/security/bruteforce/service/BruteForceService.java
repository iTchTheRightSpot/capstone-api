package com.example.sarabrandserver.security.bruteforce.service;

import com.example.sarabrandserver.security.bruteforce.entity.BruteForceEntity;
import com.example.sarabrandserver.security.bruteforce.repository.BruteForceRepo;
import com.example.sarabrandserver.user.repository.ClientRepo;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service @Setter
public class BruteForceService {
    private int MAX = 5;
    private final BruteForceRepo bruteForceRepo;
    private final ClientRepo clientRepo;

    public BruteForceService(BruteForceRepo bruteForceRepo, ClientRepo clientRepo) {
        this.bruteForceRepo = bruteForceRepo;
        this.clientRepo = clientRepo;
    }

    /**
     * Method to register every login failure of type BadCredentials for a Clientz.
     * @param auth of type Spring Core Authentication
     */
    public void registerLoginFailure(Authentication auth) {
        this.clientRepo.findByPrincipal(auth.getName()).ifPresent(clientz -> {
            var bruteEntity = this.bruteForceRepo.findByPrincipal(auth.getName());
            if (bruteEntity.isEmpty()) {
                this.bruteForceRepo.save(new BruteForceEntity(0, auth.getName()));
                return;
            }

            if (bruteEntity.get().getFailedAttempt() >= this.MAX) {
                this.clientRepo.lockClientz(false, clientz.getClientId());
                // TODO send client email to change password
                return;
            }

            // Increment
            bruteEntity.get().setFailedAttempt(bruteEntity.get().getFailedAttempt() + 1);
            this.bruteForceRepo.update(bruteEntity.get());
        });
    }

    /**
     * Method to delete the BruteForceEntity after a successful login.
     * @param auth of type Spring Core Authentication
     */
    public void resetBruteForceCounter(Authentication auth) {
        this.clientRepo.findByPrincipal(auth.getName()) //
                .flatMap(clientz -> this.bruteForceRepo.findByPrincipal(auth.getName())) //
                .ifPresent(entity -> this.bruteForceRepo.delete(entity.getPrincipal()));
    }

}
