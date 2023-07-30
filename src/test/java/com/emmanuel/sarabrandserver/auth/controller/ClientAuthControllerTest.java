package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.user.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ClientAuthControllerTest {
    private final String USERNAME = "SEJU Development";
    private final String PASSWORD = "123#-SEJU-Development";

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private ClientRoleRepo clientRoleRepo;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthService authService;
    @Autowired private CustomUtil customUtil;

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
        this.customUtil.setMaxSession(1);
        var dto = RegisterDTO.builder()
                .firstname("SEUY")
                .lastname("Development")
                .email("SEJU@development.com")
                .username(USERNAME)
                .phone("0000000000")
                .password(PASSWORD)
                .build();
        this.authService.clientRegister(dto);
    }

    @AfterEach
    void tearDown() {
        this.clientRoleRepo.deleteAll();
        this.userRepository.deleteAll();
    }

    /* Simulates login with username instead of email */
    @Test @Order(1)
    void login() throws Exception {
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(new LoginDTO(USERNAME, PASSWORD).toJson().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        var cookie = login.getResponse().getCookie(JSESSIONID);

        assertNotNull(cookie);

        this.MOCK_MVC
                .perform(get("/test/client").cookie(cookie))
                .andExpect(status().isOk());
    }

    /** Max session is 1 */
    @Test @Order(2)
    void validateMaxSession() throws Exception {
        // Browser 1
        MvcResult login1 = this.MOCK_MVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(new LoginDTO(USERNAME, PASSWORD).toJson().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        // Browser 2
        MvcResult login2 = this.MOCK_MVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(new LoginDTO(USERNAME, PASSWORD).toJson().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        // Should return 401
        var cookie = login1.getResponse().getCookie(JSESSIONID);
        assertNotNull(cookie);
        this.MOCK_MVC
                .perform(get("/test/client").cookie(cookie))
                .andExpect(status().isUnauthorized());

        // Should return 200
        cookie = login2.getResponse().getCookie(JSESSIONID);
        assertNotNull(cookie);
        this.MOCK_MVC
                .perform(get("/test/client").cookie(cookie))
                .andExpect(status().isOk());
    }

}