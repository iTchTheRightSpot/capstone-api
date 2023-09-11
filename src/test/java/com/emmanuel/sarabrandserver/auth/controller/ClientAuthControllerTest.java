package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.user.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientAuthControllerTest extends AbstractIntegrationTest {
    private final String PRINCIPAL = "SEJU@development.com";
    private final String PASSWORD = "123#-SEJU-Development";

    @Value(value = "${server.servlet.session.cookie.name}") private String JSESSIONID;
    @Autowired private ClientRoleRepo clientRoleRepo;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthService authService;

    @BeforeEach
    void setUp() {
        var dto = RegisterDTO.builder()
                .firstname("SEUY")
                .lastname("Development")
                .email(PRINCIPAL)
                .username("")
                .phone("0000000000")
                .password(PASSWORD)
                .build();
        this.authService.clientRegister(dto);
    }

    @AfterEach
    void tearDown() {
        this.clientRoleRepo.deleteAll();
        this.userRepository.deleteAll();
    }

    /* Simulates login with username instead of email */
    @Test @Order(1)
    void login() throws Exception {
        MvcResult login = this.MOCKMVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(new LoginDTO(PRINCIPAL, PASSWORD)))
                )
                .andExpect(status().isOk())
                .andReturn();

        var cookie = login.getResponse().getCookie(JSESSIONID);

        assertNotNull(cookie);

        this.MOCKMVC
                .perform(get("/test/client").cookie(cookie))
                .andExpect(status().isOk());
    }

}