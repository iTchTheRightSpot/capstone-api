package com.sarabrandserver.category.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.data.TestingData;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientCategoryControllerTest extends AbstractIntegrationTest {

    private final String requestParam = "/api/v1/client/category";

    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private WorkerProductService workerProductService;
    @Autowired private ProductRepo productRepo;

    /** Persist dummy data on start of application */
    @BeforeEach
    void setUp() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            set.add(new Faker().commerce().department() + i);
        }

        // Persist categories
        set.forEach((s) -> this.workerCategoryService.create(new CategoryDTO(s, true, "")));

        // Persist Products
        int i = 0;
        for (String cat : set) {
            var sizeInv = TestingData.sizeInventoryDTOArray(5);
            var res = TestingData.getResult(
                    sizeInv,
                    new Faker().commerce().productName() + i,
                    cat,
                    new Faker().commerce().color()
            );
            this.workerProductService.create(res.dto(), res.files());
            i += 1;
        }
    }

    @AfterEach
    void tearDown() {
        this.productRepo.deleteAll();
        this.categoryRepository.deleteAll();
    }

    @Test
    @DisplayName(value = "Fetch all categories")
    void allCategories() throws Exception {
        this.MOCKMVC
                .perform(get(requestParam).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", notNullValue()))
                .andExpect(jsonPath("$[*].category_id", notNullValue()));
    }

    @Test
    @DisplayName(value = "Fetch all Products by category id")
    void fetchProductByCategory() throws Exception {
        // Given
        var list = this.categoryRepository.findAll();
        assertFalse(list.isEmpty());

        this.MOCKMVC
                .perform(get(requestParam + "/products")
                        .param("category_id", list.get(0).getUuid())
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].name", notNullValue()))
                .andExpect(jsonPath("$.content[*].desc", notNullValue()))
                .andExpect(jsonPath("$.content[*].product_id", notNullValue()));
    }

}