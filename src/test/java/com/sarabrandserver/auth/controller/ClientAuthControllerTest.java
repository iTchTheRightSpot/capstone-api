package com.sarabrandserver.auth.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientAuthControllerTest extends AbstractIntegrationTest {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    /* Simulates login with username instead of email */
    @Test
    @Order(1)
    void register_login() throws Exception {
        String PRINCIPAL = "fresh@prince.com";
        String PASSWORD = "password123#";

        var registerDTO = new RegisterDTO(
                "SEUY",
                "Development",
                PRINCIPAL,
                "",
                "0000000000",
                PASSWORD
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
        Cookie c = register.getResponse().getCookie(JSESSIONID);
        assertNotNull(c);

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