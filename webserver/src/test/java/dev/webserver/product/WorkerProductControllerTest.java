package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.TestData;
import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerProductControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}worker/product")
    private String path;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository productDetailRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    void dummy() {
        var category = categoryRepository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .build()
                );

        TestData.dummyProducts(category, 2, workerProductService);

        var clothes = categoryRepository
                .save(
                        Category.builder()
                                .name("clothes")
                                .isVisible(true)
                                .build()
                );

        TestData.dummyProducts(clothes, 5, workerProductService);
    }

    private String productName() {
        var list = TestUtility.toList(productRepository.findAll());
        assertFalse(list.isEmpty());
        return list.getFirst().name();
    }

    private long categoryId() {
        var list = TestUtility.toList(categoryRepository.findAll());
        assertFalse(list.isEmpty());
        return list.getFirst().categoryId();
    }

    private String colour() {
        var list = TestUtility.toList(productDetailRepository.findAll());
        assertFalse(list.isEmpty());
        return list.getFirst().colour();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allProducts() throws Exception {
        // given
        var category = categoryRepository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .build()
                );

        TestData.dummyProducts(category, 50, workerProductService);

        // https://stackoverflow.com/questions/42069226/mockmvc-perform-post-test-to-async-service
        // perform the asynchronous call
        super.mockMvc
                .perform(get(path)
                        .param("page", "0")
                        .param("size", "30")
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content.size()").value(20));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyCreateAProduct() throws Exception {
        dummy();

        // payload
        SizeInventoryDto[] dtos = {
                new SizeInventoryDto(10, "small"),
                new SizeInventoryDto(3, "medium"),
                new SizeInventoryDto(15, "large"),
        };

        var dto = TestData
                .createProductDTO(
                        new Faker().commerce().productName(),
                        categoryId(),
                        dtos
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.mapper.writeValueAsString(dto).getBytes()
        );

        // request
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(path).file(payload);

        for (MockMultipartFile file : TestData.files()) {
            requestBuilder.file(file);
        }

        super.mockMvc
                .perform(requestBuilder.contentType(MULTIPART_FORM_DATA).with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void validateProductIsCreatedWhenSizeInventoryDTOArrayIsOne() throws Exception {
        dummy();

        // given
        var dto = TestData
                .createProductDTO(
                        new Faker().commerce().productName(),
                        categoryId(),
                        new SizeInventoryDto[]{ new SizeInventoryDto(10, "small") }
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                super.mapper.writeValueAsString(dto).getBytes()
        );

        // request
        MockMultipartHttpServletRequestBuilder builder = multipart(path).file(payload);

        for (MockMultipartFile file : TestData.files()) {
            builder.file(file);
        }

        super.mockMvc
                .perform(builder
                        .contentType(MULTIPART_FORM_DATA)
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldThrowErrorBecauseProductColourIsADuplicate() throws Exception {
        dummy();

        // Given
        SizeInventoryDto[] dtos = {
                new SizeInventoryDto(10, "small"),
                new SizeInventoryDto(3, "medium"),
                new SizeInventoryDto(15, "large"),
        };

        var dto = TestData
                .productDTO(
                        categoryId(),
                        productName(),
                        dtos,
                        colour()
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.mapper.writeValueAsString(dto).getBytes()
        );

        // Then
        MockMultipartHttpServletRequestBuilder builder = multipart(path).file(payload);

        for (MockMultipartFile file : TestData.files()) {
            builder.file(file);
        }

        super.mockMvc
                .perform(builder
                        .contentType(MULTIPART_FORM_DATA)
                        .with(csrf())
                )
                .andExpect(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldThrowErrorAsSizeInventoryIsNotPresentInRequest() throws Exception {
        dummy();

        var dto = TestData
                .productDTO(
                        categoryId(),
                        new Faker().commerce().productName(),
                        null,
                        colour()
                );

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                this.mapper.writeValueAsString(dto).getBytes()
        );

        // Then
        MockMultipartHttpServletRequestBuilder builder = multipart(path).file(payload);

        for (MockMultipartFile file : TestData.files()) {
            builder.file(file);
        }

        this.mockMvc
                .perform(builder
                        .contentType(MULTIPART_FORM_DATA)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldThrowErrorAsProductNameExists() throws Exception {
        dummy();

        // Given
        var product = TestUtility.toList(productRepository.findAll());
        assertFalse(product.isEmpty());
        assertTrue(product.size() > 2);

        var category = TestUtility.toList(categoryRepository.findAll());
        assertFalse(category.isEmpty());

        // Payload
        var dto = TestData
                .updateProductDTO(
                        product.get(0).uuid(),
                        product.get(1).name(),
                        category.getFirst().categoryId()
                );

        // Then
        super.mockMvc
                .perform(put(path)
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isConflict())
                .andDo(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyUpdateAProduct() throws Exception {
        dummy();

        // given
        var product = TestUtility.toList(productRepository.findAll());
        assertFalse(product.isEmpty());

        var category = TestUtility.toList(categoryRepository.findAll());
        assertFalse(category.isEmpty());

        // Payload
        var dto = TestData
                .updateProductDTO(
                        product.getFirst().uuid(),
                        "SEJU Development",
                        category.getFirst().categoryId()
                );

        // Then
        super.mockMvc
                .perform(put(path)
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteProductButExceptionIsThrownDueToResourcesAttached() throws Exception {
        dummy();

        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());
        var product = products.getFirst();
        assertNotNull(product);

        super.mockMvc
                .perform(delete(path)
                        .param("id", product.uuid())
                        .with(csrf())
                )
                .andExpect(result -> assertInstanceOf(ResourceAttachedException.class, result.getResolvedException()));

        assertFalse(productRepository.findById(product.productId()).isEmpty());
    }

}