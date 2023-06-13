package com.example.sarabrandserver.security.bruteforce;

import com.example.sarabrandserver.user.repository.ClientRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Setter @Slf4j
public class BruteForceService {
    private int MAX = 5;
    private final BruteForceRepo bruteForceRepo;
    private final ClientRepository clientRepository;

    public BruteForceService(BruteForceRepo bruteForceRepo, ClientRepository clientRepository) {
        this.bruteForceRepo = bruteForceRepo;
        this.clientRepository = clientRepository;
    }

    /**
     * Method to register every login failure of type BadCredentials for a Clientz.
     * @param auth of type Spring Core Authentication
     */
    @Transactional
    public void registerLoginFailure(Authentication auth) {
        log.info("MAX BEFORE BEING BLOCKED {}", this.MAX);
        var clientz = this.clientRepository.findByPrincipal(auth.getName()).orElse(null);
        
        if (clientz == null || !clientz.isAccountNoneLocked()) {
            log.info("In IF Client {}", clientz.isAccountNoneLocked());
            return;
        }

        log.info("Client is none locked? {}", clientz.isAccountNoneLocked());
        
        // Using redis for caching and concurrency
        var bruteEntity = this.bruteForceRepo.findByPrincipal(auth.getName());

        if (bruteEntity.isEmpty()) {
            this.bruteForceRepo.save(new BruteForceEntity(0, auth.getName()));
            return;
        }

        if (bruteEntity.get().getFailedAttempt() < this.MAX) {
            bruteEntity.get().setFailedAttempt(bruteEntity.get().getFailedAttempt() + 1);
            this.bruteForceRepo.update(bruteEntity.get());
            return;
        }

        this.clientRepository.lockClientAccount(false, clientz.getClientId());
        // TODO send client email to change password
    }


    /**
     * Method to delete the BruteForceEntity after a successful login.
     * @param auth of type Spring Core Authentication
     */
    public void resetBruteForceCounter(Authentication auth) {
        this.clientRepository.findByPrincipal(auth.getName()) //
                .flatMap(clientz -> this.bruteForceRepo.findByPrincipal(auth.getName())) //
                .ifPresent(entity -> this.bruteForceRepo.delete(entity.getPrincipal()));
    }

}
