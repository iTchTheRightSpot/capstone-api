package com.sarabrandserver.product.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.product.dto.UpdateProductDetailDTO;
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

class WorkerProductDetailControllerTest extends AbstractIntegrationTest {

    private final String requestMapping = "/api/v1/worker/product/detail";

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
            Simulates fetching ProductDetails by product uuid.
            Main objective is to validate native sql query
            """)
    void fetchAllDetail() throws Exception {
        // Given
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());

        // Based on setUp
        this.MOCKMVC
                .perform(get(requestMapping)
                        .param("id", list.get(0).getUuid())
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());

        // payload
        MockMultipartFile[] files = TestingData.files("");

        var dtos = TestingData.sizeInventoryDTOArray(5);

        String productID = list.get(0).getUuid();
        var dto = TestingData.productDetailDTO(productID, "exon-mobile-colour", dtos);

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.MAPPER.writeValueAsString(dto).getBytes()
        );

        // request
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(requestMapping).file(payload);

        for (MockMultipartFile file : TestingData.files("")) {
            requestBuilder.file(file);
        }

        this.MOCKMVC
                .perform(requestBuilder.contentType(MULTIPART_FORM_DATA).with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Update ProductDetail")
    void updateDetail() throws Exception {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());

        String sku = list.get(0).getSku();
        var dto = new UpdateProductDetailDTO(
                sku,
                new Faker().commerce().color(),
                false,
                new Faker().number().numberBetween(2, 10),
                "large"
        );

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        var findDetail = this.productSkuRepo.findBySku(sku).orElse(null);

        assertNotNull(findDetail);

        assertEquals(dto.qty(), findDetail.getInventory());
        assertEquals(dto.size(), findDetail.getSize());
    }

}