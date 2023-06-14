package com.example.sarabrandserver.test;

import com.example.sarabrandserver.user.entity.ClientRole;
import com.example.sarabrandserver.user.entity.Clientz;
import com.example.sarabrandserver.user.repository.ClientRepository;
import com.example.sarabrandserver.user.repository.ClientRoleRepo;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static com.example.sarabrandserver.enumeration.RoleEnum.CLIENT;
import static com.example.sarabrandserver.enumeration.RoleEnum.WORKER;

@Service @Slf4j
public class TestService {

    @Bean @Transactional
    public CommandLineRunner commandLineRunner(ClientRepository repo, ClientRoleRepo role) {
        return args -> {
            repo.deleteAll();
            role.deleteAll();

            var client = Clientz.builder()
                    .firstname("Test User")
                    .lastname("Yes i")
                    .email(new Faker().internet().emailAddress())
                    .username(new Faker().name().username())
                    .phoneNumber("000-000-0000")
                    .password("password")
                    .enabled(true)
                    .credentialsNonExpired(true)
                    .accountNonExpired(true)
                    .accountNoneLocked(true)
                    .clientRole(new HashSet<>())
                    .build();

            client.addRole(new ClientRole(CLIENT));
            client.addRole(new ClientRole(WORKER));

//            repo.save(client);

            repo.findAll().forEach(e -> {
                log.info("");
                log.info("Role ID %s, Role Enum ID %s".formatted(e.getClientId(), e.getClientRole().stream().findFirst().get().getRoleId()));
            });


            var client1 = Clientz.builder()
                    .firstname("Fred User")
                    .lastname("Yes i")
                    .email("FrankCastle@a.com")
                    .username("Fred username")
                    .phoneNumber("000-000-0000")
                    .password("password")
                    .enabled(true)
                    .credentialsNonExpired(true)
                    .accountNonExpired(true)
                    .accountNoneLocked(true)
                    .clientRole(new HashSet<>())
                    .build();
            client1.addRole(new ClientRole(CLIENT));

//            repo.save(client1);
        };
    }

}
