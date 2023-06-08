package com.example.sarabrandserver.auth.controller;

import com.example.sarabrandserver.auth.service.AuthService;
import com.example.sarabrandserver.dto.LoginDTO;
import com.example.sarabrandserver.exception.DuplicateException;
import com.example.sarabrandserver.security.CustomStrategy;
import com.example.sarabrandserver.worker.dto.WorkerRegisterDTO;
import com.example.sarabrandserver.worker.repository.WorkerRepo;
import com.example.sarabrandserver.worker.repository.WorkerRoleRepo;
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
class WorkerAuthControllerTest {

    private final String ADMIN_EMAIL = "SEJU@development.com";

    private final String USERNAME = "test username";

    private final String ADMIN_PASSWORD = "123#-SEJU-Development";

    @Value(value = "${custom.cookie.name}") private String COOKIE_NAME;

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private CustomStrategy customStrategy;

    @Autowired private AuthService authService;

    @Autowired private WorkerRepo workerRepo;

    @Autowired private WorkerRoleRepo workerRoleRepo;

    @Container
    private static final MySQLContainer<?> container;

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
        this.authService.workerRegister(new WorkerRegisterDTO(
                "James",
                ADMIN_EMAIL,
                USERNAME,
                ADMIN_PASSWORD
        ));
    }

    @AfterEach
    void tearDown() {
        this.workerRoleRepo.deleteAll();
        this.workerRepo.deleteAll();
    }

    /** Method does two things in one. Login and Register. To register, worker has to have a role WORKER */
    @Test @Order(1)
    void register() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/auth/worker/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        // Register
        var dto = new WorkerRegisterDTO(
                "James",
                "james@james.com",
                "james development",
                "A;D@#$13245eifdkj"
        );
        this.MOCK_MVC
                .perform(post("/api/v1/auth/worker/register")
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .cookie(login.getResponse().getCookie(COOKIE_NAME))
                )
                .andExpect(status().isCreated());
    }

    @Test @Order(2)
    void register_with_existing_credentials() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/auth/worker/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        var dto = new WorkerRegisterDTO(
                "James",
                ADMIN_EMAIL,
                USERNAME,
                ADMIN_PASSWORD
        );

        this.MOCK_MVC
                .perform(post("/api/v1/auth/worker/register").contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .cookie(login.getResponse().getCookie(COOKIE_NAME))
                )
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException))
                .andExpect(result -> assertEquals(
                        dto.email() + " exists",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
    }

    @Test @Order(3)
    void logout() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/auth/worker/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        // Cookie
        Cookie cookie = login.getResponse().getCookie(COOKIE_NAME);

        // Logout
        this.MOCK_MVC.perform(get("/api/v1/auth/logout").cookie(cookie))
                .andExpect(status().isOk());

        // Verify cookie is invalid
        this.MOCK_MVC.perform(get("/test/worker").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                )
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));
    }

}