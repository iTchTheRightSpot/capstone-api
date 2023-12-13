package com.sarabrandserver.product.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkerProductControllerTest extends AbstractIntegrationTest {

    private final String requestMapping = "/api/v1/worker/product";

    private String productName() {
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());
        return list.get(0).getName();
    }

    private String category() {
        var list = this.categoryRepository.findAll();
        assertFalse(list.isEmpty());
        return list.get(0).getCategoryName();
    }

    private String colour() {
        var list = this.productDetailRepo.findAll();
        assertFalse(list.isEmpty());
        return list.get(0).getColour();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Simulates fetching all Products")
    void fetchAll() throws Exception {
        // Then
        this.MOCKMVC
                .perform(get(requestMapping)
                        .param("page", "0")
                        .param("size", "50")
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Create a product")
    void create() throws Exception {
        category();

        // payload
        SizeInventoryDTO[] dtos = {
                new SizeInventoryDTO(10, "small"),
                new SizeInventoryDTO(3, "medium"),
                new SizeInventoryDTO(15, "large"),
        };

        var dto = TestingData
                .createProductDTOCollectionNotPresent(
                        new Faker().commerce().productName(),
                        category(),
                        "",
                        dtos
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.MAPPER.writeValueAsString(dto).getBytes()
        );

        // request
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(requestMapping).file(payload);

        for (MockMultipartFile file : TestingData.files()) {
            requestBuilder.file(file);
        }

        this.MOCKMVC
                .perform(requestBuilder.contentType(MULTIPART_FORM_DATA).with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Validate SizeInventoryDTO[] size is 1")
    void val() throws Exception {
        // given
        var dto = TestingData
                .createProductDTOCollectionNotPresent(
                        new Faker().commerce().productName(),
                        category(),
                        "",
                        new SizeInventoryDTO[]{ new SizeInventoryDTO(10, "small") }
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.MAPPER.writeValueAsString(dto).getBytes()
        );

        // request
        MockMultipartHttpServletRequestBuilder builder = multipart(requestMapping).file(payload);

        for (MockMultipartFile file : TestingData.files()) {
            builder.file(file);
        }

        this.MOCKMVC
                .perform(builder
                        .contentType(MULTIPART_FORM_DATA)
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
    Validates duplicate exception is thrown on creation of a new product.
    Exception is cause from duplicate product colour
    """)
    void ex() throws Exception {
        // Given
        SizeInventoryDTO[] dtos = {
                new SizeInventoryDTO(10, "small"),
                new SizeInventoryDTO(3, "medium"),
                new SizeInventoryDTO(15, "large"),
        };

        var dto = TestingData
                .productDTO(
                        category(),
                        "",
                        productName(),
                        dtos,
                        colour()
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.MAPPER.writeValueAsString(dto).getBytes()
        );

        // Then
        MockMultipartHttpServletRequestBuilder builder = multipart(requestMapping).file(payload);

        for (MockMultipartFile file : TestingData.files()) {
            builder.file(file);
        }

        this.MOCKMVC
                .perform(builder
                        .contentType(MULTIPART_FORM_DATA)
                        .with(csrf())
                )
                .andExpect(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Validates bad request because sizeInventory JsonProperty is not present")
    void exThrown() throws Exception {
        var dto = TestingData
                .productDTO(
                        category(),
                        "",
                        new Faker().commerce().productName(),
                        null,
                        colour()
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.MAPPER.writeValueAsString(dto).getBytes()
        );

        // Then
        MockMultipartHttpServletRequestBuilder builder = multipart(requestMapping).file(payload);

        for (MockMultipartFile file : TestingData.files()) {
            builder.file(file);
        }

        this.MOCKMVC
                .perform(builder
                        .contentType(MULTIPART_FORM_DATA)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Update Product. Ex thrown because product name exists")
    void updateEx() throws Exception {
        // Given
        var product = this.productRepo.findAll();
        assertFalse(product.isEmpty());
        assertTrue(product.size() > 2);

        var category = this.categoryRepository.findAll();
        assertFalse(category.isEmpty());

        // Payload
        var dto = TestingData
                .updateProductDTO(
                        product.get(0).getUuid(),
                        product.get(1).getName(),
                        category.get(0).getCategoryName(),
                        category.get(0).getUuid(),
                        "",
                        ""
                );

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isConflict())
                .andDo(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Update Product. Category and Collection are in the payload")
    void updateProduct() throws Exception {
        // Given
        var product = this.productRepo.findAll();
        assertFalse(product.isEmpty());

        var category = this.categoryRepository.findAll();
        assertFalse(category.isEmpty());

        var collection = this.collectionRepository.findAll();
        assertFalse(collection.isEmpty());

        // Payload
        var dto = TestingData
                .updateProductDTO(
                        product.get(0).getUuid(),
                        "SEJU Development",
                        category.get(0).getCategoryName(),
                        category.get(0).getUuid(),
                        collection.get(0).getCollection(),
                        collection.get(0).getUuid()
                );

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Update Product. Collection and collection_id are empty payload")
    void updateCol() throws Exception {
        // Given
        var product = this.productRepo.findAll();
        assertFalse(product.isEmpty());

        var category = this.categoryRepository.findAll();
        assertFalse(category.isEmpty());

        // payload
        var dto = TestingData
                .updateProductDTO(
                        product.get(0).getUuid(),
                        "SEJU Development",
                        category.get(0).getCategoryName(),
                        category.get(0).getUuid(),
                        "",
                        ""
                );

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteProduct() throws Exception {
        var product = this.productRepo.findAll().stream().findFirst().orElse(null);
        assertNotNull(product);

        this.MOCKMVC
                .perform(delete(requestMapping)
                        .param("id", product.getUuid())
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        var del = this.productRepo.findById(product.getProductId()).orElse(null);
        assertNull(del);
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Delete Product. Exception thrown because product has more than 1 ProductDetail ")
    void deleteProductEx() throws Exception {
        var product = this.productRepo.findAll().stream().findFirst().orElse(null);
        assertNotNull(product);

        this.MOCKMVC.perform(delete(requestMapping).param("id", product.getUuid()).with(csrf()))
                .andExpect(status().isNoContent());

        var del = this.productRepo.findById(product.getProductId()).orElse(null);
        assertNull(del);
    }

}