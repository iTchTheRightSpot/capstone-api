package com.emmanuel.sarabrandserver.category.controller;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.github.javafaker.Faker;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Class simulates testing basic CRUD functionalities. It doesn't go depth of testing errors throw etc. because this is
 * done in  unit testing class.
 * */
@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class WorkerCategoryControllerTest {

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private WorkerCategoryService workerCategoryService;

    @Autowired private CategoryRepository categoryRepository;

    private CategoryDTO categoryDTO;

    private final static String requestMapping = "/api/v1/worker/category";

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
        Set<String> parentCategory = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            parentCategory.add(new Faker().commerce().department());
        }

        for (String str : parentCategory) {
            this.categoryDTO = new CategoryDTO(str, true, "");
            this.workerCategoryService.create(this.categoryDTO);
        }
    }

    @AfterEach
    void tearDown() {
        this.categoryRepository.deleteAll();
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void fetchCategories() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(get(requestMapping).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void create() throws Exception {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, "");

        // Then
        this.MOCK_MVC
                .perform(post(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                )
                .andExpect(status().isCreated());
    }

    /** Simulates creating a new Category with param parent in CategoryDTO non-empty */
    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void create1() throws Exception {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, this.categoryDTO.getName());

        // Then
        this.MOCK_MVC
                .perform(post(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                )
                .andExpect(status().isCreated());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void update() throws Exception {
        // Given
        long id = this.categoryRepository.findAll().get(0).getCategoryId();
        var dto = new UpdateCategoryDTO(id, "Updated category name");

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                )
                .andExpect(status().isOk());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void custom_delete() throws Exception {
        this.MOCK_MVC
                .perform(delete(requestMapping + "/{name}", this.categoryDTO.getName())
                        .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

}