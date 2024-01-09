package com.sarabrandserver.category.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientCategoryControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}client/category")
    private String requestParam;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductSkuRepo productSkuRepo;
    @Autowired
    private ProductDetailRepo productDetailRepo;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void before() {
        var category = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestingData.dummyProducts(category, 2, workerProductService);

        var clothes = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestingData.dummyProducts(clothes, 5, workerProductService);
    }

    @AfterEach
    void after() {
        productSkuRepo.deleteAll();
        productDetailRepo.deleteAll();
        productRepo.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void allCategories() throws Exception {
        this.MOCKMVC
                .perform(get(requestParam).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", notNullValue()))
                .andExpect(jsonPath("$[*].category_id", notNullValue()));
    }

    @Test
    @DisplayName(value = "All Products by categoryId id")
    void productByCategory() throws Exception {
        // Given
        var list = this.categoryRepository.findAll();
        assertFalse(list.isEmpty());

        this.MOCKMVC
                .perform(get(requestParam + "/products")
                        .param("category_id", String.valueOf(list.getFirst().getCategoryId()))
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].name", notNullValue()))
                .andExpect(jsonPath("$.content[*].desc", notNullValue()))
                .andExpect(jsonPath("$.content[*].product_id", notNullValue()));
    }

}