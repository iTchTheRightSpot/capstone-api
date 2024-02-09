package com.sarabrandserver.auth.controller;

import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.auth.dto.LoginDto;
import com.sarabrandserver.auth.dto.RegisterDto;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.enumeration.RoleEnum;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkerAuthControllerTest extends AbstractIntegration {

    private final String PRINCIPAL = "SEJU@development.com";
    private final String PASSWORD = "123#-SEJU-Development";

    @Value(value = "/${api.endpoint.baseurl}worker/auth/")
    private String route;
    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    @Autowired
    private UserRepository repository;
    @Autowired
    private AuthService service;

    @AfterEach
    void after() {
        repository.deleteAll();
    }

    /**
     * Method does two things in one. Login and Register. To register, worker has to have a role WORKER
     */
    @Test
    @Order(1)
    void register() throws Exception {
        // given
        this.service.register(
                null,
                new RegisterDto(
                "SEJU",
                "Development",
                PRINCIPAL,
                "",
                "000-000-0000",
                PASSWORD
                ),
                RoleEnum.WORKER
        );

        // when
        MvcResult login = this.MOCKMVC
                .perform(post(route + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(new LoginDto(PRINCIPAL, PASSWORD)))
                )
                .andExpect(status().isOk())
                .andReturn();

        // Register
        var dto = new RegisterDto(
                "James",
                "james@james.com",
                "james@james.com",
                "james development",
                "0000000000",
                "A;D@#$13245eifdkj"
        );

        this.MOCKMVC
                .perform(post(route + "register")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .cookie(login.getResponse().getCookie(JSESSIONID))
                )
                .andExpect(status().isCreated());
    }

    /**
     * Simulates registering an existing worker
     */
    @Test
    @Order(2)
    void register_with_existing_credentials() throws Exception {
        // given
        this.service.register(
                null,
                new RegisterDto(
                        "SEJU",
                        "Development",
                        PRINCIPAL,
                        "",
                        "000-000-0000",
                        PASSWORD
                ),
                RoleEnum.WORKER
        );

        // when
        MvcResult login = this.MOCKMVC
                .perform(post(route + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(new LoginDto(PRINCIPAL, PASSWORD)))
                )
                .andExpect(status().isOk())
                .andReturn();

        var dto = new RegisterDto(
                "SEJU",
                "Development",
                PRINCIPAL,
                "",
                "00-000-0000",
                PASSWORD
        );

        this.MOCKMVC
                .perform(post(route + "register")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(this.MAPPER.writeValueAsString(dto))
                        .cookie(login.getResponse().getCookie(JSESSIONID))
                )
                .andExpect(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @Order(3)
    void login_wrong_password() throws Exception {
        // given
        this.service.register(
                null,
                new RegisterDto(
                        "SEJU",
                        "Development",
                        PRINCIPAL,
                        "",
                        "000-000-0000",
                        PASSWORD
                ),
                RoleEnum.WORKER
        );

        // when
        String payload = this.MAPPER
                .writeValueAsString(new LoginDto(PRINCIPAL, "fFeubfrom@#$%^124234"));
        this.MOCKMVC
                .perform(post(route + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(payload)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    /**
     * Validates cookie has been cleared.
     */
    @Test
    @Order(4)
    void logout() throws Exception {
        // given
        this.service.register(
                null,
                new RegisterDto(
                        "SEJU",
                        "Development",
                        PRINCIPAL,
                        "",
                        "000-000-0000",
                        PASSWORD
                ),
                RoleEnum.WORKER
        );

        // then
        MvcResult login = this.MOCKMVC
                .perform(post(route + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(new LoginDto(PRINCIPAL, PASSWORD)))
                )
                .andExpect(status().isOk())
                .andReturn();

        // Jwt Cookie
        Cookie cookie = login.getResponse().getCookie(JSESSIONID);
        assertNotNull(cookie);

        // Logout
        MvcResult logout = this.MOCKMVC
                .perform(post("/api/v1/logout").cookie(cookie).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        cookie = logout.getResponse().getCookie(JSESSIONID); // This should be empty

        // Access protected route with invalid cookie
        this.MOCKMVC
                .perform(get("/test/worker").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                );
    }

}