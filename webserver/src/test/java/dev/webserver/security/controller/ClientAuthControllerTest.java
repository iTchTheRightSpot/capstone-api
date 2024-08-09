package dev.webserver.security.controller;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.security.CapstoneUserDetails;
import dev.webserver.security.JwtUtil;
import dev.webserver.user.SarreBrandUser;
import dev.webserver.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientAuthControllerTest extends AbstractIntegration {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String jsessionid;
    @Value(value = "/${api.endpoint.baseurl}client/auth/")
    private String path;
    @Value(value = "${jwt.claim}")
    private String claim;
    @Value("${spring.application.name}")
    private String application;

    private SarreBrandUser user = null;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtEncoder jwtEncoder;

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
                        .content(this.mapper.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getCookie(jsessionid);

    }

    @Test
    void shouldSuccessfullyLogin() throws Exception {
        String dto = super.mapper.writeValueAsString(new LoginDto(user.email(), "password123"));

        MvcResult login = super.mockMvc
                .perform(post(path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                )
                .andExpect(status().isOk())
                .andExpect(cookie().exists(jsessionid))
                .andReturn();

        super.mockMvc
                .perform(get("/test/client").cookie(login.getResponse().getCookie(jsessionid)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPreventLoginRequestIfRequestContainsValidJwt() throws Exception {
        String dto = super.mapper.writeValueAsString(new LoginDto(user.email(), "password123"));

        MvcResult login = super.mockMvc
                .perform(post(path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(jsessionid))
                .andReturn();

        super.mockMvc
                .perform(post(path + "login")
                        .with(csrf())
                        .cookie(login.getResponse().getCookie(jsessionid))
                        .contentType(APPLICATION_JSON)
                        .content(dto))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(jsessionid))
                .andExpect(cookie().value(jsessionid, Objects.requireNonNull(login.getResponse().getCookie(jsessionid)).getValue()));
    }

    private String generateShortLivedJwt() {
        Instant now = Instant.now();

        String[] role = new CapstoneUserDetails(user).getAuthorities() //
                .stream() //
                .map(authority -> JwtUtil.substringAfter(authority.getAuthority(), "ROLE_"))
                .toArray(String[]::new);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(application)
                .issuedAt(now)
                .expiresAt(now.plus(2, MINUTES))
                .subject(user.email())
                .claim(claim, role)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Test
    void shouldValidateRefreshTokenIsAddedToJwtThatIsWithinExpirationBound() throws Exception {
        String jwt = generateShortLivedJwt();

        String dto = super.mapper.writeValueAsString(new LoginDto(user.email(), "password123"));

        super.mockMvc
                .perform(post(path + "login")
                        .with(csrf())
                        .cookie(new Cookie(jsessionid, jwt))
                        .contentType(APPLICATION_JSON)
                        .content(dto))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(jsessionid))
                .andExpect((result) -> assertThat(jwt).isNotEqualTo(Objects.requireNonNull(result.getResponse().getCookie(jsessionid)).getValue()));
    }

    @Test
    void simulate_logging_in_with_none_existent_user() throws Exception {
        String dto = super.mapper
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
        String dto = this.mapper.writeValueAsString(new LoginDto(user.email(), "password123"));

        MvcResult login = this.mockMvc
                .perform(post(this.path + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                )
                .andExpect(status().isOk())
                .andReturn();

        var cookie = login.getResponse().getCookie(jsessionid);

        assertNotNull(cookie);

        mockMvc.perform(get("/test/worker").cookie(cookie)).andExpect(status().isForbidden());
    }

}