package com.sarabrandserver;

import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class DummyData {

    @Bean
    CommandLineRunner commandLineRunner(AuthService service, UserRepository repository) {
        return args -> {
            if (repository.findByPrincipal("admin@admin.com").isPresent()) {
                return;
            }
            var dto = new RegisterDTO(
                    "SEJU",
                    "Development",
                    "admin@admin.com",
                    "admin@admin.com",
                    "0000000000",
                    "password123"
            );
            service.workerRegister(dto);
        };
    }

}
