package com.sarabrandserver.product.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.product.repository.ProductRepository;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.util.Result;
import com.sarabrandserver.util.TestingData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientProductControllerTest extends AbstractIntegrationTest {

    private final String requestMapping = "/api/v1/client/product";

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
        int detailSize = 1;
        SizeInventoryDTO[] sizeInventoryDTO1 = TestingData.sizeInventoryDTOArray(detailSize);
        Result result = TestingData.getResult(
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

//        MvcResult result = MOCKMVC
//                .perform(get(requestMapping + "/detail")
//                        .param("product_id", productID)
//                )
//                .andExpect(request().asyncStarted())
//                .andDo(MockMvcResultHandlers.log())
//                .andReturn();

//        MOCKMVC
//                .perform(asyncDispatch(result))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$[*].variants").isArray())
//                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));

        this.MOCKMVC
                .perform(get(requestMapping + "/detail")
                        .param("product_id", productID)
                )
                .andExpect(content().contentType(TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
    }

}