package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.clientz.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
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
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ClientAuthControllerTest {

    private final String ADMIN_EMAIL = "SEJU@development.com";
    private final String ADMIN_USERNAME = "SEJU Development";

    private final String ADMIN_PASSWORD = "123#-SEJU-Development";

    @Value(value = "${custom.cookie.name}") private String COOKIE_NAME;

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private ClientRoleRepo clientRoleRepo;

    @Autowired private ClientzRepository clientzRepository;

    @Autowired private AuthService authService;

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
        this.authService.clientRegister(new RegisterDTO(
                "SEJU",
                "Development",
                ADMIN_EMAIL,
                ADMIN_USERNAME,
                "00-000-0000",
                ADMIN_PASSWORD
        ));
    }

    @AfterEach
    void tearDown() {
        this.clientRoleRepo.deleteAll();
        this.clientzRepository.deleteAll();
    }

    @Test @Order(1)
    void register() throws Exception {
        var dto = new RegisterDTO(
                "firstname",
                "Development",
                "yes@yes.com",
                "yes development",
                "00-000-0000",
                ADMIN_PASSWORD
        );

        this.MOCK_MVC
                .perform(post("/api/v1/client/auth/register").contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

    @Test @Order(2)
    void register_with_existing_credentials() throws Exception {
        var dto = new RegisterDTO(
                "SEJU",
                "Development",
                ADMIN_EMAIL,
                ADMIN_USERNAME,
                "00-000-0000",
                ADMIN_PASSWORD
        );

        this.MOCK_MVC
                .perform(post("/api/v1/client/auth/register").contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException))
                .andExpect(result -> assertEquals(
                        dto.getEmail() + " exists",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
    }

    @Test @Order(3)
    void login() throws Exception {
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        this.MOCK_MVC
                .perform(get("/test/client").cookie(login.getResponse().getCookie(COOKIE_NAME)))
                .andExpect(status().isOk());
    }

    @Test @Order(4)
    void login_wrong_password() throws Exception {
        this.MOCK_MVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, "fFeubfrom@#$%^124234").convertToJSON().toString())
                )
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadCredentialsException))
                .andExpect(result -> assertEquals(
                        "Bad credentials",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
//                .andExpect(jsonPath("$.message").value("Bad credentials"))
//                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));
    }

    @Test @Order(5)
    void logout() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(ADMIN_EMAIL, ADMIN_PASSWORD).convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = login.getResponse().getCookie(COOKIE_NAME);

        // Logout
        this.MOCK_MVC.perform(post("/api/v1/auth/logout").cookie(cookie).with(csrf()))
                .andExpect(status().isOk());
    }

}