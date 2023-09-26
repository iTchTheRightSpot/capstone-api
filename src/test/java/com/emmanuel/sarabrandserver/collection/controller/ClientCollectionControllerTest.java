package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.service.WorkerProductService;
import com.emmanuel.sarabrandserver.util.TestingData;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientCollectionControllerTest extends AbstractIntegrationTest {

    private final String requestParam = "/api/v1/client/collection";

    @Autowired private WorkerCategoryService categoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private WorkerCollectionService collectionService;
    @Autowired private CollectionRepository collectionRepository;
    @Autowired private WorkerProductService workerProductService;
    @Autowired private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // Persist Category
        String category = new Faker().commerce().department();
        this.categoryService
                .create(new CategoryDTO(category, true, ""));

        // Persist collections
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            set.add(new Faker().commerce().department());
        }
        set.forEach(e -> this.collectionService.create(new CollectionDTO(e, true)));

        // Persist Products
        int i = 0;
        for (String col : set) {
            var sizeInv = TestingData.sizeInventoryDTOArray(5);
            var res = TestingData.getResultCollection(
                    col,
                    sizeInv,
                    new Faker().commerce().productName() + i,
                    category,
                    new Faker().commerce().color()
            );
            this.workerProductService.create(res.dto(), res.files());
            i += 1;
        }
    }

    @AfterEach
    void tearDown() {
        this.categoryRepository.deleteAll();
        this.productRepository.deleteAll();
        this.collectionRepository.deleteAll();
    }

    @Test
    void allCollections() throws Exception {
        this.MOCKMVC
                .perform(get(requestParam).contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].collection", notNullValue()))
                .andExpect(jsonPath("$[*].collection_id", notNullValue()));
    }

    @Test
    @DisplayName(value = "Fetch all Products by collection id")
    void fetchProductByCollection() throws Exception {
        // Given
        var list = this.collectionRepository.findAll();
        assertFalse(list.isEmpty());

        this.MOCKMVC
                .perform(get(requestParam + "/product")
                        .param("collection_id", list.get(0).getUuid())
                        .contentType(APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].name", notNullValue()))
                .andExpect(jsonPath("$.content[*].desc", notNullValue()))
                .andExpect(jsonPath("$.content[*].product_id", notNullValue()));
    }

}