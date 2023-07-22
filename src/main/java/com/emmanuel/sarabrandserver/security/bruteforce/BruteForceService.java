package com.emmanuel.sarabrandserver.security.bruteforce;

import com.emmanuel.sarabrandserver.user.repository.ClientzRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Prevents against brute force attack
     * @param auth of type Spring Core Authentication
     */
    @Transactional
    public void loginFailure(Authentication auth) {
        var client = this.clientzRepository.findByPrincipal(auth.getName()).orElse(null);

        if (client == null) {
            return;
        }

        var bruteForceEntity = this.bruteForceRepo.findByPrincipal(auth.getName()).orElse(null);
        if (bruteForceEntity == null) {
            this.bruteForceRepo.save(new BruteForceEntity(0, auth.getName()));
            return;
        }

        if (bruteForceEntity.getFailedAttempt() >= this.MAX) {
            this.clientzRepository.lockClientAccount(false, client.getClientId());
            return;
        }

        bruteForceEntity.setFailedAttempt(bruteForceEntity.getFailedAttempt() + 1);
        this.bruteForceRepo.save(bruteForceEntity);
    }


    /**
     * Method to delete the BruteForceEntity after a successful login.
     * @param auth of type Spring Core Authentication
     */
    public void resetBruteForceCounter(Authentication auth) {
        this.clientzRepository
                .findByPrincipal(auth.getName()) //
                .flatMap(clientz -> this.bruteForceRepo.findByPrincipal(auth.getName())) //
                .ifPresent(entity -> this.bruteForceRepo.delete(entity.getPrincipal()));
    }

}
