package com.sarabrandserver.user.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerControllerTest extends AbstractIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        var dto = new RegisterDTO(
                "SEUY",
                "Development",
                "test1@hello.com",
                "",
                "2220003366",
                "password123"
        );
        this.authService.clientRegister(dto);

        var dto1 = new RegisterDTO(
                "SEUY",
                "Development",
                "test2@hello.com",
                "",
                "0000000000",
                "password123"
        );
        this.authService.clientRegister(dto1);
    }

    @AfterEach
    void tearDown() {
        this.userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allUsers() throws Exception {
        this.MOCKMVC
                .perform(get("/api/v1/worker/user")
                        .param("page", "0")
                        .param("size", "20")
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.length()").value(2));
    }

}