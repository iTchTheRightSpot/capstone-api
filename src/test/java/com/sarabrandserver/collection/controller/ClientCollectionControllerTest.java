package com.sarabrandserver.collection.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientCollectionControllerTest extends AbstractIntegrationTest {

    private final String requestParam = "/api/v1/client/collection";

    @Test
    void allCollections() throws Exception {
        this.MOCKMVC
                .perform(get(requestParam).contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].collection", notNullValue()))
                .andExpect(jsonPath("$[*].collection_id", notNullValue()));
    }

    @Test
    @DisplayName(value = "Fetch all Products by collection id")
    void fetchProductByCollection() throws Exception {
        // Given
        var list = this.collectionRepository.findAll();
        assertFalse(list.isEmpty());

        this.MOCKMVC
                .perform(get(requestParam + "/products")
                        .param("collection_id", list.get(0).getUuid())
                        .contentType(APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].name", notNullValue()))
                .andExpect(jsonPath("$.content[*].desc", notNullValue()))
                .andExpect(jsonPath("$.content[*].product_id", notNullValue()));
    }

}