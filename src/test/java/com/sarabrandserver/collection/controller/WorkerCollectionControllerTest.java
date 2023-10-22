package com.sarabrandserver.collection.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.collection.dto.CollectionDTO;
import com.sarabrandserver.collection.dto.UpdateCollectionDTO;
import com.sarabrandserver.exception.DuplicateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerCollectionControllerTest extends AbstractIntegrationTest {

    private final String requestMapping = "/api/v1/worker/collection";

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        // payload
        var dto = new CollectionDTO(new Faker().commerce().productName(), true);

        // request
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
    @DisplayName(value = "validates updating a ProductCollection")
    void update() throws Exception {
        // payload
        var collection = this.collectionRepository.findAll().get(0);
        var dto = new UpdateCollectionDTO(collection.getUuid(), "RandomName", true);

        // request
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
    @DisplayName(value = "validates custom query throws exception when updating a ProductCollection")
    void ex() throws Exception {
        // Given
        var collections = this.collectionRepository.findAll();

        assertFalse(collections.isEmpty());

        // First collections
        var first = collections.get(0);
        // second collections
        var second = collections.get(1);
        // dto
        var dto = new UpdateCollectionDTO(first.getUuid(), second.getCollection(), true);

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

}