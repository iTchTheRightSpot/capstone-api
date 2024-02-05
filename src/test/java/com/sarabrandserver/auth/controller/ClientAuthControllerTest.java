package com.sarabrandserver.auth.controller;

import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientAuthControllerTest extends AbstractIntegration {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;
    @Value(value = "/${api.endpoint.baseurl}client/auth/")
    private String path;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void after() {
        userRepository.deleteAll();
    }

    Cookie cookie(String principal, String password) throws Exception {
        var dto = new RegisterDTO(
                "SEUY",
                "Development",
                principal,
                "",
                "0000000000",
                password
        );

        return this.MOCKMVC
                .perform(post(this.path + "register")
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getCookie(JSESSIONID);
    }

    @Test
    @Order(1)
    void register_and_login() throws Exception {
        String principal = "fresh@prince.com";
        String password = "password123#";

        Cookie c = cookie(principal, password);
        assertNotNull(c);

        String dto = this.MAPPER.writeValueAsString(new LoginDTO(principal, password));

        MvcResult login = this.MOCKMVC
                .perform(post(this.path + "login")
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
    void simulate_logging_in_with_none_existent_user() throws Exception {
        String dto = this.MAPPER
                .writeValueAsString(new LoginDTO("admin@admin.com", "password123"));
        this.MOCKMVC
                .perform(post(this.path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void client_trying_to_access_worker_route() throws Exception {
        String principal = "freshprince@prince.com";
        String password = "password123#";

        Cookie c = cookie(principal, password);
        assertNotNull(c);

        String dto = this.MAPPER.writeValueAsString(new LoginDTO(principal, password));

        MvcResult login = this.MOCKMVC
                .perform(post(this.path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                )
                .andExpect(status().isOk())
                .andReturn();

        var cookie = login.getResponse().getCookie(JSESSIONID);

        assertNotNull(cookie);

        this.MOCKMVC
                .perform(get("/test/worker").cookie(cookie))
                .andExpect(status().isForbidden());
    }

}