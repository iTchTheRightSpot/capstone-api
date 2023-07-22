package com.emmanuel.sarabrandserver.jwt;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.clientz.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Class performs integration tests haha */
@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class JwtTokenServiceTest {
    @Value(value = "${server.servlet.session.cookie.name}")
    private String COOKIE_NAME;

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private JwtTokenService jwtTokenService;
    @Autowired private AuthService authService;
    @Autowired private ClientRoleRepo clientRoleRepo;
    @Autowired private ClientzRepository clientzRepository;

    @Container
    private static final MySQLContainer<?> container;

    static {
        container = new MySQLContainer<>("mysql:latest")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");
    }

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @BeforeEach
    void setUp() {
        this.jwtTokenService.setTokenExpiry(20);
        this.jwtTokenService.setBoundToSendRefreshToken(20);

        this.authService.workerRegister(new RegisterDTO(
                "SEJU",
                "Development",
                "admin@admin.com",
                "admin@admin.com",
                "0000000000",
                "password"
        ));
    }

    @AfterEach
    void tearDown() {
        this.clientRoleRepo.deleteAll();
        this.clientzRepository.deleteAll();
    }

    /** Method validates the logic of sending refresh token in CustomFilter class. */
    @Test
    public void validateRefreshToken() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO("admin@admin.com", "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        // Test routes
        this.MOCK_MVC.perform(get("/test/client").cookie(login.getResponse().getCookie(COOKIE_NAME)))
                .andExpect(status().isOk());

        this.MOCK_MVC.perform(get("/test/worker").cookie(login.getResponse().getCookie(COOKIE_NAME)))
                .andExpect(status().isOk());
    }

}