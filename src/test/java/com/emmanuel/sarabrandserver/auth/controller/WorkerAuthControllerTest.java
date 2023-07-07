package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.clientz.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.security.bruteforce.BruteForceService;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
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
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Slf4j
class WorkerAuthControllerTest {
    @Value(value = "${custom.cookie.name}")
    private String COOKIE_NAME;

    private final int MAX_FAILED_AUTH = 3;
    private final String ADMIN_EMAIL = "SEJU@development.com";
    private final String USERNAME = "SEJU Development";
    private final String ADMIN_PASSWORD = "123#-SEJU-Development";

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private AuthService authService;
    @Autowired private ClientRoleRepo clientRoleRepo;
    @Autowired private ClientzRepository clientzRepository;
    @Autowired private BruteForceService bruteForceService;

    @Container private static final MySQLContainer<?> container;

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
        this.bruteForceService.setMAX(MAX_FAILED_AUTH);
        this.authService.workerRegister(new RegisterDTO(
                "SEJU",
                "Development",
                ADMIN_EMAIL,
                USERNAME,
                "000-000-0000",
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
                .andReturn();

        // Register
        var dto = new RegisterDTO(
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
                .andReturn();

        var dto = new RegisterDTO(
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException));
//                .andExpect(result -> assertEquals(
//                        dto.getEmail() + " exists",
//                        Objects.requireNonNull(result.getResolvedException()).getMessage()
//                ));
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
                .andReturn();

        // Test route
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

    /** Validates cookie has been clear. But cookie will still be valid if it due to jwt being stateless */
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