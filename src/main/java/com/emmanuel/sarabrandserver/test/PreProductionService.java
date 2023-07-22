package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.user.repository.ClientzRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile(value = {"stage", "dev"})
public class PreProductionService {

    @Bean
    public CommandLineRunner commandLineRunner(AuthService aSer, ClientzRepository cRepo) {
        return args -> {
            if (cRepo.findByPrincipal("admin@admin.com").isPresent()) {
                return;
            }
            aSer.workerRegister(new RegisterDTO(
                    "SEJU",
                    "Development",
                    "admin@admin.com",
                    "StageAdmin",
                    "0000000000",
                    "password123"
            ));
        };
    }

}
