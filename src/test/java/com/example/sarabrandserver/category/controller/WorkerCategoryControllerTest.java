package com.example.sarabrandserver.category.controller;

import com.example.sarabrandserver.category.dto.CategoryDTO;
import com.example.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import com.example.sarabrandserver.category.service.WorkerCategoryService;
import com.example.sarabrandserver.exception.DuplicateException;
import com.github.javafaker.Faker;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO Refactor tests because an update
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

    private final int max = 3;

    private final int subMax = 5;

    private final Set<String> parentCategory = new HashSet<>();

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
        for (int i = 0; i < max; i++) {
            parentCategory.add(new Faker().commerce().productName());
        }

        for (String str : parentCategory) {
            this.categoryDTO = new CategoryDTO(str, true, new HashSet<>());

            for (int i = 0; i < subMax; i++) {
                this.categoryDTO.getSub_category().add(UUID.randomUUID().toString());
            }

            this.workerCategoryService.create(this.categoryDTO);
        }

    }

    @AfterEach
    void tearDown() {
        this.categoryRepository.deleteAll();
    }

    @Test
    void allCategories() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(get("/api/v1/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].category_name").isArray())
                .andExpect(jsonPath("$.[*].category_name")
                        .value(hasSize(this.parentCategory.size())))
                .andExpect(jsonPath("$.[*].sub_category").isArray());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void create() throws Exception {
        // Given
        var dto = new CategoryDTO(new Faker().name().lastName(), true, new HashSet<>());
        for (int i = 0; i < 20; i++) dto.getSub_category().add(UUID.randomUUID().toString());

        // Then
        this.MOCK_MVC
                .perform(post("/api/v1/category")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                )
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void create_existing() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(post("/api/v1/category")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.categoryDTO.toJson().toString())
                )
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException))
                .andExpect(result -> assertEquals(
                        "Duplicate name or sub category",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void update() throws Exception {
        // Given
        var dto = UpdateCategoryDTO.builder()
                .old_name(this.categoryDTO.getCategory_name())
                .new_name("Updated category name")
                .build();

        // Then
        this.MOCK_MVC
                .perform(put("/api/v1/category")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                )
                .andExpect(status().isOk());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void custom_delete() throws Exception {
        this.MOCK_MVC
                .perform(delete("/api/v1/category/{category_id}", this.categoryDTO.getCategory_name())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Validate delete request
        this.MOCK_MVC
                .perform(get("/api/v1/category"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].category_name").isArray())
                .andExpect(jsonPath("$.[*].category_name", hasSize(parentCategory.size() - 1)));
    }

}