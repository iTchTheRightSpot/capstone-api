package com.sarabrandserver.category.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientCategoryControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}client/category")
    private String requestParam;

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
    void productByCategory() throws Exception {
        // Given
        var list = this.categoryRepository.findAll();
        assertFalse(list.isEmpty());

        this.MOCKMVC
                .perform(get(requestParam + "/products")
                        .param("category_id", String.valueOf(list.get(0).getCategoryId()))
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].name", notNullValue()))
                .andExpect(jsonPath("$.content[*].desc", notNullValue()))
                .andExpect(jsonPath("$.content[*].product_id", notNullValue()));
    }

}