package com.sarabrandserver.auth.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.user.repository.UserRepository;
import com.sarabrandserver.user.repository.UserRoleRepository;
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

    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthService authService;

    @BeforeEach
    void setUp() {
        var dto = new RegisterDTO(
                "SEUY",
                "Development",
                PRINCIPAL,
                "",
                "0000000000",
                PASSWORD
        );
        this.authService.clientRegister(dto);
    }

    @AfterEach
    void tearDown() {
        this.userRoleRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    /* Simulates login with username instead of email */
    @Test @Order(1)
    void login() throws Exception {
        String dto = this.MAPPER.writeValueAsString(new LoginDTO(PRINCIPAL, PASSWORD));

        MvcResult login = this.MOCKMVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
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