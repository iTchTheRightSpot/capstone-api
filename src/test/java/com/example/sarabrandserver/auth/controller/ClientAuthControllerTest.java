package com.example.sarabrandserver.auth.controller;

import com.example.sarabrandserver.auth.service.AuthService;
import com.example.sarabrandserver.client.dto.ClientRegisterDTO;
import com.example.sarabrandserver.client.repository.ClientRepo;
import com.example.sarabrandserver.client.repository.ClientRoleRepo;
import com.example.sarabrandserver.dto.LoginDTO;
import com.example.sarabrandserver.exception.DuplicateException;
import com.example.sarabrandserver.security.CustomStrategy;
import com.redis.testcontainers.RedisContainer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
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
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class ClientAuthControllerTest {

    private final String ADMIN_EMAIL = "SEJU@development.com";

    private final String ADMIN_PASSWORD = "123#-SEJU-Development";

    @Value(value = "${custom.cookie.name}") private String COOKIE_NAME;

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private ClientRoleRepo clientRoleRepo;

    @Autowired private ClientRepo clientRepo;

    @Autowired private AuthService authService;

    @Autowired private CustomStrategy customStrategy;

    @Container private static final MySQLContainer<?> container;

    @Container private static final RedisContainer redis;

    static {
        container = new MySQLContainer<>("mysql:latest")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");

        redis = new RedisContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);
    }

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        this.customStrategy.setMAX_SESSION(1);
        this.authService.clientRegister(new ClientRegisterDTO(
                "SEJU",
                "Development",
                ADMIN_EMAIL,
                "00-000-0000",
                ADMIN_PASSWORD
        ));
    }

    @AfterEach
    void tearDown() {
        this.clientRoleRepo.deleteAll();
        this.clientRepo.deleteAll();
    }

    @Test @Order(1)
    void register() throws Exception {
        var dto = new ClientRegisterDTO(
                "firstname",
                "Development",
                "yes@yes.com",
                "00-000-0000",
                ADMIN_PASSWORD
        );

        this.MOCK_MVC
                .perform(post("/api/v1/auth/client/register").contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                )
                .andExpect(status().isCreated());
    }

    @Test @Order(2)
    void register_with_existing_credentials() throws Exception {
        var dto = new ClientRegisterDTO(
                "SEJU",
                "Development",
                ADMIN_EMAIL,
                "00-000-0000",
                ADMIN_PASSWORD
        );

        this.MOCK_MVC
                .perform(post("/api/v1/auth/client/register").contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException))
                .andExpect(result -> assertEquals(
                        dto.email() + " exists",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
    }

    @Test @Order(3)
    void login() throws Exception {
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/auth/client/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        this.MOCK_MVC
                .perform(get("/test/client").cookie(login.getResponse().getCookie(COOKIE_NAME)))
                .andExpect(status().isOk());
    }

    @Test @Order(4)
    void login_wrong_password() throws Exception {
        this.MOCK_MVC
                .perform(post("/api/v1/auth/client/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, "fFeubfrom@#$%^124234").convertToJSON().toString())
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));
    }

    @Test @Order(5)
    void logout() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/auth/client/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = login.getResponse().getCookie(COOKIE_NAME);

        // Logout
        this.MOCK_MVC.perform(get("/api/v1/auth/logout").cookie(cookie))
                .andExpect(status().isOk());

        // Verify cookie is invalid
        this.MOCK_MVC.perform(get("/test/client").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                )
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));
    }

    /** Max session is 1 */
    @Test @Order(6)
    void validate_max_session() throws Exception {
        // Browser 1
        MvcResult login_one = this.MOCK_MVC
                .perform(post("/api/v1/auth/client/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie_one = login_one.getResponse().getCookie(COOKIE_NAME);

        // Browser 2
        MvcResult login_two = this.MOCK_MVC
                .perform(post("/api/v1/auth/client/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie_two = login_two.getResponse().getCookie(COOKIE_NAME);

        // Should return 401
        this.MOCK_MVC
                .perform(get("/test/client").cookie(cookie_one))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                )
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));

        // Should return 200
        this.MOCK_MVC
                .perform(get("/test/client").cookie(cookie_two))
                .andExpect(status().isOk());
    }

}