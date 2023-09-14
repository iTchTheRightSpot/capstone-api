package com.emmanuel.sarabrandserver.integration;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerCategoryControllerTest extends AbstractIntegrationTest {

    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;

    private CategoryDTO categoryDTO;

    private final static String requestMapping = "/api/v1/worker/category";

    @BeforeEach
    void setUp() {
        Set<String> parentCategory = new HashSet<>();
        for (int i = 0; i < 10; i++) {
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

    @Test @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void fetchCategories() throws Exception {
        // Then
        this.MOCKMVC
                .perform(get(requestMapping).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
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

    /** Simulates creating a new Category with param parent in CategoryDTO non-empty */
    @Test @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
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
    @DisplayName(value = "validates updating a product")
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
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "validates custom query throws exception when updating a product")
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

    @Test @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void custom_delete() throws Exception {
        this.MOCKMVC
                .perform(delete(requestMapping + "/{name}", this.categoryDTO.getName()).with(csrf()))
                .andExpect(status().isNoContent());
    }

}