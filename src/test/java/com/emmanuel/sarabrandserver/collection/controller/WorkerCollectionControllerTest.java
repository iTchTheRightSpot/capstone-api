package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.UpdateCollectionDTO;
import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerCollectionControllerTest extends AbstractIntegrationTest {
    private final static String requestMapping = "/api/v1/worker/collection";

    @Autowired private WorkerCollectionService collectionService;
    @Autowired private CollectionRepository collectionRepository;

    @BeforeEach
    void setUp() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            set.add(new Faker().commerce().department());
        }

        // Save dummy data pre tests
        set.forEach(e -> this.collectionService.create(new CollectionDTO(e, true)));
    }

    @AfterEach
    void tearDown() {
        this.collectionRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        // payload
        var dto = new CollectionDTO(new Faker().commerce().productName(), true);

        // request
        this.MOCKMVC.perform(post(requestMapping)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.MAPPER.writeValueAsString(dto))
        ).andExpect(status().isCreated());
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
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "validates custom query throws exception when updating a ProductCollection")
    void ex() throws Exception {
        // Given
        var category = this.collectionRepository.findAll();
        // First category
        var first = category.get(0);
        // second category
        var second = category.get(1);
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