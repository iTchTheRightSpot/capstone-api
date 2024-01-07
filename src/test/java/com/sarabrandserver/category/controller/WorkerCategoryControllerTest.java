package com.sarabrandserver.category.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.exception.ResourceAttachedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerCategoryControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}worker/category")
    private String requestMapping;

    private String category() {
        var list = this.categoryRepository.findAll();
        assertFalse(list.isEmpty());
        return list.get(0).getName();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allCategories() throws Exception {
        // Then
        this.MOCKMVC
                .perform(get(requestMapping).contentType(APPLICATION_JSON))
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
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, category());

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
    @DisplayName(value = "Testing updating a ProductCategory")
    void update() throws Exception {
        // Given
        var category = this.categoryRepository.findAll().get(0);
        var dto = new UpdateCategoryDTO(category.getCategoryId(), "Updated", category.isVisible());

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
        var dto = new UpdateCategoryDTO(first.getCategoryId(), second.getName(), first.isVisible());

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isConflict())
                .andExpect(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "delete ProductCategory when it has no Product attached")
    void deleteEx() throws Exception {
        var category = this.categoryRepository.findByName(category()).orElse(null);
        assertNotNull(category);

        this.MOCKMVC
                .perform(delete(requestMapping + "/{id}", category.getCategoryId())
                        .with(csrf())
                )
                .andExpect(status().isConflict())
                .andDo(result -> assertInstanceOf(ResourceAttachedException.class, result.getResolvedException()));
    }

}