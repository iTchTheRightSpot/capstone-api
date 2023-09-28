package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.service.WorkerProductService;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.emmanuel.sarabrandserver.util.Result;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.emmanuel.sarabrandserver.util.TestingData.getResult;
import static com.emmanuel.sarabrandserver.util.TestingData.sizeInventoryDTOArray;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientProductControllerTest extends AbstractIntegrationTest {
    private final static String requestMapping = "/api/v1/client/product";

    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Create and save category
        String category = new Faker().commerce().department();
        this.workerCategoryService.create(new CategoryDTO(category, true, ""));

        // Create and save Product
        SizeInventoryDTO[] sizeInventoryDTO1 = sizeInventoryDTOArray(1);
        Result result = getResult(
                sizeInventoryDTO1,
                new Faker().commerce().productName(),
                category,
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
    @DisplayName(value = "Testing getting ProductDetail by product id only this is a SSeEmitter")
    void fetchProductDetails() throws Exception {
        var list = this.productRepository.findAll();
        assertFalse(list.isEmpty());

        String productID = list.get(0).getUuid();

        this.MOCKMVC
                .perform(get(requestMapping + "/detail")
                        .param("product_id", productID)
                )
                .andExpect(content().contentType(TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
    }

}