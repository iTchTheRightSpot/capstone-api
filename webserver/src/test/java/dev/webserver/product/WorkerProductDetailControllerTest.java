package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.category.CategoryRepository;
import dev.webserver.category.ProductCategory;
import dev.webserver.data.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerProductDetailControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}worker/product/detail")
    private String path;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSkuRepository productSkuRepository;
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
        var list = this.productRepository.findAll();
        assertFalse(list.isEmpty());

        // based on setUp
        super.mockMvc.perform(get(path).param("id", list.getFirst().getUuid())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyCreateAProductDetail() throws Exception {
        var list = this.productRepository.findAll();
        assertFalse(list.isEmpty());

        // payload
        var dtos = TestData.sizeInventoryDTOArray(5);

        String productID = list.getFirst().getUuid();
        var dto = TestData.productDetailDTO(productID, "exon-mobile-colour", dtos);

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.mapper.writeValueAsString(dto).getBytes()
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
        var list = this.productSkuRepository.findAll();
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
                        .content(this.mapper.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        var findDetail = this.productSkuRepository.productSkuBySku(sku).orElse(null);

        assertNotNull(findDetail);

        assertEquals(dto.qty(), findDetail.getInventory());
        assertEquals(dto.size(), findDetail.getSize());
    }

}