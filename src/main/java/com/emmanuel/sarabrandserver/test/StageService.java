package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile(value = {"stage"})
public class StageService {

    @Bean
    public CommandLineRunner commandLineRunner(AuthService authService, ClientzRepository clientzRepository) {
        return args -> {
            if (clientzRepository.findByPrincipal("admin@admin.com").isPresent()) {
                return;
            }
            authService.workerRegister(new RegisterDTO(
                    "Admin Test",
                    "Development",
                    "admin@admin.com",
                    "StageAdmin",
                    "0000000000",
                    "password123"
            ));
        };
    }
}
