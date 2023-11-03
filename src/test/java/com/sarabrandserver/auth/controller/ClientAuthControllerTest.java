package com.sarabrandserver.auth.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.user.repository.UserRepository;
import com.sarabrandserver.user.repository.UserRoleRepository;
import jakarta.servlet.http.Cookie;
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

    private final String ADMIN = "admin@admin.com";
    private final String PASSWORD = "password123";

    @Value(value = "${server.servlet.session.cookie.name}") private String JSESSIONID;

    @Autowired private AuthService authService;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        var dto = new RegisterDTO(
                "SEJU",
                "Development",
                ADMIN,
                "",
                "000-000-0000",
                PASSWORD
        );
        this.authService.workerRegister(dto);
    }

    @AfterEach
    void tearDown() {
        this.userRoleRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    Cookie cookie(String principal, String password) throws Exception {

        var registerDTO = new RegisterDTO(
                "SEUY",
                "Development",
                principal,
                "",
                "0000000000",
                password
        );

        String payload = this.MAPPER.writeValueAsString(registerDTO);

        MvcResult register = this.MOCKMVC
                .perform(post("/api/v1/client/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(payload)
                        .with(csrf())
                )
                .andExpect(status().isCreated())
                .andReturn();

        // assert jwt cookie is present after registration
        return register.getResponse().getCookie(JSESSIONID);
    }

    @Test
    @Order(1)
    void register_login() throws Exception {
        String principal = "fresh@prince.com";
        String password = "password123#";

        Cookie c = cookie(principal, password);
        assertNotNull(c);

        String dto = this.MAPPER.writeValueAsString(new LoginDTO(principal, password));

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

    @Test
    @Order(2)
    void simulate_logging_in_with_jwt_cookie_present() throws Exception {
        Cookie c = cookie("fresh@prince.com", "password123#");

        String dto = this.MAPPER.writeValueAsString(new LoginDTO(ADMIN, PASSWORD));

        this.MOCKMVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                        .cookie(c)
                )
                .andExpect(status().isOk());
    }

}