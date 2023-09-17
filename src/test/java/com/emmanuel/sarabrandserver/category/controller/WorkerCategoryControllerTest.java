package com.emmanuel.sarabrandserver.category.controller;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.exception.ResourceAttachedException;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.emmanuel.sarabrandserver.product.worker.WorkerProductService;
import com.emmanuel.sarabrandserver.util.Result;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashSet;
import java.util.Set;

import static com.emmanuel.sarabrandserver.util.TestingData.getResult;
import static com.emmanuel.sarabrandserver.util.TestingData.sizeInventoryDTOArray;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerCategoryControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WorkerCategoryService workerCategoryService;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepository productRepository;

    private CategoryDTO categoryDTO;

    private final static String requestMapping = "/api/v1/worker/category";
    private final StringBuilder category = new StringBuilder();

    @BeforeEach
    void setUp() {
        Set<String> parentCategory = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            parentCategory.add(new Faker().commerce().department());
        }

        parentCategory.forEach(str -> {
            if (this.category.isEmpty()) {
                this.category.append(str);
            }
            this.categoryDTO = new CategoryDTO(str, true, "");
            this.workerCategoryService.create(this.categoryDTO);
        });

        // Save Product
        SizeInventoryDTO[] sizeInventoryDTO1 = sizeInventoryDTOArray(2);
        Result result = getResult(
                sizeInventoryDTO1,
                new Faker().commerce().productName(),
                this.category.toString(),
                new Faker().commerce().color()
        );

        this.workerProductService.create(result.dto(), result.files());
    }

    @AfterEach
    void tearDown() {
        this.productRepository.deleteAll();
        this.categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void fetchCategories() throws Exception {
        // Then
        this.MOCKMVC
                .perform(get(requestMapping).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, "");

        // Then
        this.MOCKMVC
                .perform(post(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    /**
     * Simulates creating a new Category with param parent in CategoryDTO non-empty
     */
    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create1() throws Exception {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, this.categoryDTO.getName());

        // Then
        this.MOCKMVC
                .perform(post(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "validates updating a ProductCategory")
    void update() throws Exception {
        // Given
        var category = this.categoryRepository.findAll().get(0);
        var dto = new UpdateCategoryDTO(category.getUuid(), "Updated", category.isVisible());

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "validates custom query throws exception when updating a ProductCategory")
    void ex() throws Exception {
        // Given
        var category = this.categoryRepository.findAll();
        // First category
        var first = category.get(0);
        // second category
        var second = category.get(1);
        // dto
        var dto = new UpdateCategoryDTO(first.getUuid(), second.getCategoryName(), first.isVisible());

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "delete ProductCategory when it has no Product attached")
    void deleteCat() throws Exception {
        var categories = this.categoryRepository.findAll();
        assertTrue(categories.size() > 2);

        String uuid = categories.get(0)
                .getCategoryName()
                .contentEquals(this.category)
                ? categories.get(1).getUuid() : categories.get(0).getUuid();

        this.MOCKMVC
                .perform(delete(requestMapping + "/{uuid}", uuid)
                        .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "delete ProductCategory when it has no Product attached")
    void deleteEx() throws Exception {
        var category = this.categoryRepository.findByName(this.category.toString()).orElse(null);
        assertNotNull(category);

        this.MOCKMVC
                .perform(delete(requestMapping + "/{uuid}", category.getUuid())
                        .with(csrf())
                )
                .andExpect(status().isConflict())
                .andDo(result -> assertTrue(result.getResolvedException() instanceof ResourceAttachedException));
    }

}