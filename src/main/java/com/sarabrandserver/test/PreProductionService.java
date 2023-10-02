package com.sarabrandserver.test;

import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile(value = {"stage", "dev"})
public class PreProductionService {

    @Bean
    public CommandLineRunner commandLineRunner(AuthService aSer, UserRepository cRepo) {
        return args -> {
            if (cRepo.findByPrincipal("admin@admin.com").isPresent()) {
                return;
            }
            aSer.workerRegister(new RegisterDTO(
                    "SEJU",
                    "Development",
                    "admin@admin.com",
                    "admin@admin.com",
                    "0000000000",
                    "password123"
            ));
        };
    }

}
