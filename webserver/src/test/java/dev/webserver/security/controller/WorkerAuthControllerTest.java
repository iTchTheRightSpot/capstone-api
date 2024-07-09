package dev.webserver.security.controller;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.security.CapstoneUserDetails;
import dev.webserver.exception.DuplicateException;
import dev.webserver.security.JwtService;
import dev.webserver.user.ClientRole;
import dev.webserver.user.SarreBrandUser;
import dev.webserver.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static dev.webserver.enumeration.RoleEnum.CLIENT;
import static dev.webserver.enumeration.RoleEnum.WORKER;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerAuthControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}worker/auth/")
    private String route;
    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    private SarreBrandUser user = null;
    private String jwt = null;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
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
                            .clientRole(Set.of(new ClientRole(CLIENT), new ClientRole(WORKER, user)))
                            .paymentDetail(new HashSet<>())
                            .build());

            jwt = jwtService.generateToken(
                    authenticated(user.getEmail(), null, new CapstoneUserDetails(user).getAuthorities()));
        }
    }

    @Test
    void register() throws Exception {
        var dto = new RegisterDto(
                "James",
                "james@james.com",
                "james@james.com",
                "james development",
                "0000000000",
                "A;D@#$13245eifdkj"
        );

        this.mockMvc
                .perform(post(route + "register")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.mapper.writeValueAsString(dto))
                        .cookie(new Cookie(JSESSIONID, jwt))
                )
                .andExpect(status().isCreated());
    }

    @Test
    void register_with_existing_credentials() throws Exception {
        var dto = new RegisterDto(
                "SEJU",
                "Development",
                user.getEmail(),
                "",
                "00-000-0000",
                "password123"
        );

        this.mockMvc
                .perform(post(route + "register")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(this.mapper.writeValueAsString(dto))
                        .cookie(new Cookie(JSESSIONID, jwt))
                )
                .andExpect(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    void login_wrong_password() throws Exception {
        String payload = this.mapper
                .writeValueAsString(new LoginDto(user.getPassword(), "fFeubfrom@#$%^124234"));
        this.mockMvc
                .perform(post(route + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(payload)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    void logout() throws Exception {
        // Logout
        MvcResult logout = super.mockMvc
                .perform(post("/api/v1/logout").cookie(new Cookie(JSESSIONID, jwt)).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = logout.getResponse().getCookie(JSESSIONID); // This should be empty

        // Access protected route with invalid cookie
        super.mockMvc
                .perform(get("/test/worker").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource"));
    }

}