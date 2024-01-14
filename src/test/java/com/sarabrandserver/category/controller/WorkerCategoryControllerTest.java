package com.sarabrandserver.category.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.exception.ResourceAttachedException;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerCategoryControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}worker/category")
    private String requestMapping;

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
                                .isVisible(false)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 2, workerProductService);

        var clothes = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(false)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(clothes, 5, workerProductService);
    }

    @AfterEach
    void after() {
        productSkuRepo.deleteAll();
        productDetailRepo.deleteAll();
        productRepo.deleteAll();
        categoryRepository.deleteAll();
    }

    private ProductCategory category() {
        var list = this.categoryRepository.findAll();
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allCategories() throws Exception {
        // Then
        this.MOCKMVC
                .perform(get(requestMapping).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.table").isArray())
                .andExpect(jsonPath("$.hierarchy").isArray());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Test successfully creating a ProductCategory when parent id is null")
    void create() throws Exception {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, null);

        // Then
        this.MOCKMVC
                .perform(post(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Test successfully creating a ProductCategory when parent id isn't null")
    void create1() throws Exception {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, category().getCategoryId());

        // Then
        this.MOCKMVC
                .perform(post(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Test successfully updating a ProductCategory")
    void update() throws Exception {
        // Given
        var category = this.categoryRepository.findAll().getFirst();
        var dto = new UpdateCategoryDTO(category.getCategoryId(), null, "Updated", category.isVisible());

        // Then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "validates exception thrown from duplicate category name")
    void ex() throws Exception {
        // given
        var category = this.categoryRepository.findAll();
        var first = category.getFirst();
        var second = category.get(1);
        var dto = new UpdateCategoryDTO(first.getCategoryId(), null, second.getName(), first.isVisible());

        // then
        this.MOCKMVC
                .perform(put(requestMapping)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                )
                .andExpect(status().isConflict())
                .andExpect(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
    exception thrown when trying to delete a product because it has a
    subcategory and product attached.
    """)
    void deleteEx() throws Exception {
        var category = this.categoryRepository
                .findById(category().getCategoryId())
                .orElse(null);
        assertNotNull(category);

        this.MOCKMVC
                .perform(delete(requestMapping + "/{id}", category.getCategoryId())
                        .with(csrf())
                )
                .andExpect(status().isConflict())
                .andDo(result -> assertInstanceOf(ResourceAttachedException.class, result.getResolvedException()));
    }

}