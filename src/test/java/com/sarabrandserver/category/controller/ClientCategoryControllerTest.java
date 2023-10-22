package com.sarabrandserver.category.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class ClientCategoryControllerTest extends AbstractIntegrationTest {

    private final String requestParam = "/api/v1/client/category";

    @Test
    void allCategories() throws Exception {
        this.MOCKMVC
                .perform(get(requestParam).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", notNullValue()))
                .andExpect(jsonPath("$[*].category_id", notNullValue()));
    }

    @Test
    @DisplayName(value = "All Products by category id")
    void fetchProductByCategory() throws Exception {
        // Given
        var list = this.categoryRepository.findAll();
        assertFalse(list.isEmpty());

        this.MOCKMVC
                .perform(get(requestParam + "/products")
                        .param("category_id", list.get(0).getUuid())
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].name", notNullValue()))
                .andExpect(jsonPath("$.content[*].desc", notNullValue()))
                .andExpect(jsonPath("$.content[*].product_id", notNullValue()));
    }

}