package dev.webserver.auth.controller;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.auth.dto.LoginDto;
import dev.webserver.auth.dto.RegisterDto;
import dev.webserver.enumeration.RoleEnum;
import dev.webserver.user.entity.ClientRole;
import dev.webserver.user.entity.SarreBrandUser;
import dev.webserver.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientAuthControllerTest extends AbstractIntegration {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;
    @Value(value = "/${api.endpoint.baseurl}client/auth/")
    private String path;

    private SarreBrandUser user = null;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        if (user == null) {
            user = userRepository.save(
                    SarreBrandUser.builder()
                            .firstname(new Faker().name().firstName())
                            .lastname(new Faker().name().lastName())
                            .email(new Faker().internet().emailAddress())
                            .phoneNumber("000000000")
                            .password(encoder.encode("password123"))
                            .enabled(true)
                            .clientRole(Set.of(new ClientRole(RoleEnum.CLIENT)))
                            .paymentDetail(new HashSet<>())
                            .build());
        }
    }

    @Test
    void register() throws Exception {
        var dto = new RegisterDto(
                "SEUY",
                "Development",
                "fresh@prince.com",
                "",
                "0000000000",
                "password123#"
        );

        super.mockMvc
                .perform(post(this.path + "register")
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getCookie(JSESSIONID);

    }

    @Test
    void shouldSuccessfullyLogin() throws Exception {
        String dto = this.objectMapper.writeValueAsString(new LoginDto(user.getEmail(), "password123"));

        MvcResult login = this.mockMvc
                .perform(post(this.path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                )
                .andExpect(status().isOk())
                .andReturn();

        var cookie = login.getResponse().getCookie(JSESSIONID);

        assertNotNull(cookie);

        super.mockMvc
                .perform(get("/test/client").cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    void simulate_logging_in_with_none_existent_user() throws Exception {
        String dto = super.objectMapper
                .writeValueAsString(new LoginDto("admin@admin.com", "password123"));
        super.mockMvc
                .perform(post(this.path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void client_trying_to_access_worker_route() throws Exception {
        String dto = this.objectMapper.writeValueAsString(new LoginDto(user.getEmail(), "password123"));

        MvcResult login = this.mockMvc
                .perform(post(this.path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                )
                .andExpect(status().isOk())
                .andReturn();

        var cookie = login.getResponse().getCookie(JSESSIONID);

        assertNotNull(cookie);

        this.mockMvc
                .perform(get("/test/worker").cookie(cookie))
                .andExpect(status().isForbidden());
    }

}