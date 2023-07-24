package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.user.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.user.repository.ClientzRepository;
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

    @Value(value = "${server.servlet.session.cookie.name}") private String JSESSIONID;

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
        this.clientzRepository.deleteAll();
    }

    /* Simulates login with username instead of email */
    @Test @Order(1)
    void login() throws Exception {
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/client/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new LoginDTO(USERNAME, PASSWORD).toJson().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        this.MOCK_MVC
                .perform(get("/test/client").cookie(login.getResponse().getCookie(JSESSIONID)))
                .andExpect(status().isOk());
    }

}