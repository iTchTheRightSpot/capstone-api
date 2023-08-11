package com.emmanuel.sarabrandserver.auth.bruteforce;

import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Setter @Slf4j
public class BruteForceService {
    private int MAX = 5;
    private final BruteForceRepo bruteForceRepo;
    private final UserRepository userRepository;

    public BruteForceService(BruteForceRepo bruteForceRepo, UserRepository userRepository) {
        this.bruteForceRepo = bruteForceRepo;
        this.userRepository = userRepository;
    }

    /**
     * Prevents against brute force attack
     * @param auth of type Spring Core Authentication
     */
    @Transactional
    public void loginFailure(Authentication auth) {
        var client = this.userRepository.findByPrincipal(auth.getName()).orElse(null);

        if (client == null) {
            return;
        }

        var bruteForceEntity = this.bruteForceRepo.findByPrincipal(auth.getName()).orElse(null);
        if (bruteForceEntity == null) {
            this.bruteForceRepo.save(new BruteForceEntity(0, auth.getName()));
            return;
        }

        if (bruteForceEntity.getFailedAttempt() >= this.MAX) {
            this.userRepository.lockClientAccount(false, client.getClientId());
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
        this.userRepository
                .findByPrincipal(auth.getName()) //
                .flatMap(clientz -> this.bruteForceRepo.findByPrincipal(auth.getName())) //
                .ifPresent(entity -> this.bruteForceRepo.delete(entity.getPrincipal()));
    }

}
