package com.emmanuel.sarabrandserver.integration;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.util.Result;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.emmanuel.sarabrandserver.product.worker.WorkerProductService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.emmanuel.sarabrandserver.util.ProductTestingData.getResult;
import static com.emmanuel.sarabrandserver.util.ProductTestingData.sizeInventoryDTOArray;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientProductControllerTest extends AbstractIntegrationTest {
    private final static String requestMapping = "/api/v1/client/product";
    private final StringBuilder category = new StringBuilder();
    private final int detailSize = 1;
    private final StringBuilder productName = new StringBuilder();

    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Create and save category
        this.category.append(new Faker().commerce().department());
        this.workerCategoryService.create(new CategoryDTO(this.category.toString(), true, ""));

        // Create and save Product
        String prodName = new Faker().commerce().productName();
        this.productName.append(prodName);
        SizeInventoryDTO[] sizeInventoryDTO1 = sizeInventoryDTOArray(this.detailSize);
        Result result = getResult(
                sizeInventoryDTO1,
                prodName,
                this.category.toString(),
                new Faker().commerce().color()
        );
        this.workerService.create(result.dto(), result.files());
    }

    @AfterEach
    void tearDown() {
        this.productRepository.deleteAll();
        this.categoryRepository.deleteAll();
    }

    @Test
    @DisplayName(value = "Get ProductDetails for store front")
    void fetchProductDetails() throws Exception {
        var product = this.productRepository.findByProductName(this.productName.toString()).orElse(null);
        assertNotNull(product);
        this.MOCKMVC
                .perform(get(requestMapping + "/detail").param("id", product.getUuid()))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));
    }

}