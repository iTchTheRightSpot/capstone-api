package com.sarabrandserver.product.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.dto.UpdateProductDetailDto;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class WorkerProductDetailControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}worker/product/detail")
    private String path;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductSkuRepo productSkuRepo;
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

        TestData.dummyProducts(category, 2, workerProductService);

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

        TestData.dummyProducts(clothes, 5, workerProductService);
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void productDetailsByProductUuid() throws Exception {
        // given
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());

        // based on setUp
        MvcResult result = super.mockMvc
                .perform(get(path)
                        .param("id", list.getFirst().getUuid())
                )
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        super.mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyCreateAProductDetail() throws Exception {
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());

        // payload
        var dtos = TestData.sizeInventoryDTOArray(5);

        String productID = list.getFirst().getUuid();
        var dto = TestData.productDetailDTO(productID, "exon-mobile-colour", dtos);

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.objectMapper.writeValueAsString(dto).getBytes()
        );

        // request
        MockMultipartHttpServletRequestBuilder builder = multipart(path).file(payload);

        for (MockMultipartFile file : TestData.files()) {
            builder.file(file);
        }

        this.mockMvc
                .perform(builder.contentType(MULTIPART_FORM_DATA).with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyUpdateProductDetail() throws Exception {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());

        String sku = list.getFirst().getSku();
        var dto = new UpdateProductDetailDto(
                sku,
                new Faker().commerce().color(),
                false,
                new Faker().number().numberBetween(2, 10),
                "large"
        );

        // Then
        this.mockMvc
                .perform(put(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        var findDetail = this.productSkuRepo.productSkuBySku(sku).orElse(null);

        assertNotNull(findDetail);

        assertEquals(dto.qty(), findDetail.getInventory());
        assertEquals(dto.size(), findDetail.getSize());
    }

}