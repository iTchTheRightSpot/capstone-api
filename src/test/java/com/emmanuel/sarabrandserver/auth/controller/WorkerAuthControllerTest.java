package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.clientz.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.security.CustomStrategy;
import com.emmanuel.sarabrandserver.security.bruteforce.BruteForceService;
import com.emmanuel.sarabrandserver.clientz.dto.ClientRegisterDTO;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
class WorkerAuthControllerTest {

    private final int MAX_FAILED_AUTH = 3;

    private final String ADMIN_EMAIL = "SEJU@development.com";

    private final String USERNAME = "SEJU Development";

    private final String ADMIN_PASSWORD = "123#-SEJU-Development";

    @Value(value = "${custom.cookie.name}") private String COOKIE_NAME;

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private CustomStrategy customStrategy;

    @Autowired private AuthService authService;

    @Autowired private ClientRoleRepo clientRoleRepo;

    @Autowired private ClientzRepository clientzRepository;

    @Autowired private BruteForceService bruteForceService;

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
        this.bruteForceService.setMAX(MAX_FAILED_AUTH);
        this.authService.workerRegister(new ClientRegisterDTO(
                "SEJU",
                "Development",
                ADMIN_EMAIL,
                USERNAME,
                "00-000-0000",
                ADMIN_PASSWORD
        ));
    }

    @AfterEach
    void tearDown() {
        this.clientRoleRepo.deleteAll();
        this.clientzRepository.deleteAll();
    }

    /** Method does two things in one. Login and Register. To register, worker has to have a role WORKER */
    @Test @Order(1)
    void register() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        // Register
        var dto = new ClientRegisterDTO(
                "James",
                "james@james.com",
                "james@james.com",
                "james development",
                "0000000000",
                "A;D@#$13245eifdkj"
        );
        this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/register")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .cookie(login.getResponse().getCookie(COOKIE_NAME))
                )
                .andExpect(status().isCreated());
    }

    /** Simulates registering an existing worker */
    @Test @Order(2)
    void register_with_existing_credentials() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        var dto = new ClientRegisterDTO(
                "SEJU",
                "Development",
                ADMIN_EMAIL,
                USERNAME,
                "00-000-0000",
                ADMIN_PASSWORD
        );

        this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/register")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(dto.toJson().toString())
                        .cookie(login.getResponse().getCookie(COOKIE_NAME))
                )
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException))
                .andExpect(result -> assertEquals(
                        dto.getEmail() + " exists",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
    }

    @Test
    @Order(3)
    void login() throws Exception {
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        this.MOCK_MVC
                .perform(get("/test/worker").cookie(login.getResponse().getCookie(COOKIE_NAME)))
                .andExpect(status().isOk());
    }

    @Test @Order(4)
    void login_wrong_password() throws Exception {
        this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
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
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        // Cookie
        Cookie cookie = login.getResponse().getCookie(COOKIE_NAME);

        // Logout
        this.MOCK_MVC.perform(post("/api/v1/auth/logout").cookie(cookie).with(csrf()))
                .andExpect(status().isOk());

        // Verify cookie is invalid
        this.MOCK_MVC.perform(get("/test/worker").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                )
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));
    }

    // TODO Might be a JPA Bug
    /** Test simulates a brute force attack. The loop simulates concurrent failed login */
    @Test @Order(6)
    void attack() throws Exception {
        // Login normal
        this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(USERNAME, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk());

        // Simulate concurrent failed login attempts
        for (int i = 0; i <= MAX_FAILED_AUTH + 2; i++) {
            this.MOCK_MVC
                    .perform(post("/api/v1/worker/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new LoginDTO(ADMIN_EMAIL, "iwei36SD902#$&*").convertToJSON().toString())
                    )
                    .andExpect(status().isUnauthorized());
        }

        // locked account
        this.MOCK_MVC
                .perform(post("/api/v1/worker/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(USERNAME, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isUnauthorized());
    }

}