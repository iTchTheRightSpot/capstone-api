package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.product.service.WorkerProductService;
import com.emmanuel.sarabrandserver.product.util.UpdateProductDetailDTO;
import com.emmanuel.sarabrandserver.util.Result;
import com.emmanuel.sarabrandserver.util.TestingData;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import static com.emmanuel.sarabrandserver.util.TestingData.getResult;
import static com.emmanuel.sarabrandserver.util.TestingData.sizeInventoryDTOArray;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkerProductDetailControllerTest extends AbstractIntegrationTest {

    private final static String requestMapping = "/api/v1/worker/product/detail";
    private final int detailSize = 5;

    @Autowired private WorkerProductService workerProductService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private ProductDetailRepo productDetailRepo;
    @Autowired private ProductSkuRepo productSkuRepo;

    @BeforeEach
    void setUp() {
        // Persist category
        String category = new Faker().commerce().department();
        this.workerCategoryService.create(new CategoryDTO(category, true, ""));

        // Product Data
        Result result1 = getResult(
                sizeInventoryDTOArray(this.detailSize),
                "product1",
                category,
                "colour1"
        );

        // Persist Product 1
        this.workerProductService.create(result1.dto(), result1.files());

        // Product Data
        Result result2 = getResult(
                sizeInventoryDTOArray(this.detailSize),
                new Faker().commerce().productName(),
                category,
                new Faker().commerce().color()
        );

        // Persist Product 1
        this.workerProductService.create(result2.dto(), result2.files());
    }

    @AfterEach
    void tearDown() {
        this.productSkuRepo.deleteAll();
        this.productDetailRepo.deleteAll();
        this.productRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
            Simulates fetching ProductDetails by product uuid.
            Main objective is to validate native sql query
            """)
    void fetchAllDetail() throws Exception {
        // Given
        var list = this.productRepository.findAll();
        assertFalse(list.isEmpty());

        // Based on setUp
        this.MOCKMVC
                .perform(get(requestMapping).param("id", list.get(0).getUuid()))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        var list = this.productRepository.findAll();
        assertFalse(list.isEmpty());

        // payload
        MockMultipartFile[] files = TestingData.files(3);
        var sizeInv = sizeInventoryDTOArray(5);

        // request
        this.MOCKMVC.perform(multipart(requestMapping)
                .file(files[0])
                .file(files[1])
                .file(files[2])
                .param("uuid", list.get(0).getUuid())
                .param("visible", "false")
                .param("colour", "exon-mobile-colour")
                .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInv[0]))
                .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInv[1]))
                .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInv[2]))
                .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInv[3]))
                .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInv[4]))
                .with(csrf())
        ).andExpect(status().isCreated());
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

        assertEquals(dto.getQty(), findDetail.getInventory());
        assertEquals(dto.getSize(), findDetail.getSize());
    }

}