package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.product.dto.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.dto.ProductDTO;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class WorkerProductControllerTest {
    private final static String requestMapping = "/api/v1/worker/product";
    private String category = "";

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductDetailRepo detailRepo;

    @Container private static final MySQLContainer<?> container;

    static {
        container = new MySQLContainer<>("mysql:latest")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");
    }

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @BeforeEach
    void setUp() {
        Set<String> set = new HashSet<>();

        for (int i = 0; i < 20; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String str : set) {
            category = str;
            this.workerCategoryService.create(new CategoryDTO(str, true, ""));
        }

        for (String str : set) {
            var dto = CreateProductDTO.builder()
                    .category(str)
                    .collection("")
                    .name(new Faker().commerce().productName())
                    .desc(new Faker().lorem().characters(255))
                    .price(new BigDecimal(new Faker().commerce().price()).doubleValue())
                    .currency("USD")
                    .visible(true)
                    .qty(new Faker().number().numberBetween(10, 30))
                    .size(new Faker().commerce().material())
                    .colour(new Faker().commerce().color())
                    .build();

            MockMultipartFile[] files = {
                    new MockMultipartFile(
                            "file",
                            new Faker().file().fileName(),
                            new Faker().file().extension(),
                            new Faker().file().fileName().getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            new Faker().file().fileName(),
                            new Faker().file().extension(),
                            new Faker().file().fileName().getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            new Faker().file().fileName(),
                            new Faker().file().extension(),
                            new Faker().file().fileName().getBytes()
                    ),
            };
            this.workerService.create(dto, files);
        }

        for (int i = 0; i < 50; i++) {
            // Validate deleting
            var dto1 = CreateProductDTO.builder()
                    .category(category)
                    .collection("")
                    .name("custom-product")
                    .desc(new Faker().lorem().characters(255))
                    .price(new BigDecimal(new Faker().commerce().price()).doubleValue())
                    .currency("USD")
                    .visible(true)
                    .qty(new Faker().number().numberBetween(10, 30))
                    .size(new Faker().commerce().material())
                    .colour(new Faker().commerce().color())
                    .build();

            MockMultipartFile[] files1 = {
                    new MockMultipartFile(
                            "file",
                            new Faker().file().fileName(),
                            new Faker().file().extension(),
                            new Faker().file().fileName().getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            new Faker().file().fileName(),
                            new Faker().file().extension(),
                            new Faker().file().fileName().getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            new Faker().file().fileName(),
                            new Faker().file().extension(),
                            new Faker().file().fileName().getBytes()
                    ),
            };
            this.workerService.create(dto1, files1);
        }
    }

    @AfterEach
    void tearDown() {
        this.productRepository.deleteAll();
        this.categoryRepository.deleteAll();
    }

    /** Testing fetchAll method that returns a ProductResponse. */
    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void fetchAll() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(get(requestMapping)
                        .param("page", "0")
                        .param("size", "50")
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /** Testing fetchAll method that returns a DetailResponse. */
    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void fetchAllDetail() throws Exception {
        this.MOCK_MVC
                .perform(get(requestMapping + "/{name}", "custom-product")
                        .param("page", "0")
                        .param("size", "50")
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(30)));
    }

//    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
//    void create() throws Exception {
//        // Given
//        var dto = CreateProductDTO.builder()
//                .category(category)
//                .collection("")
//                .name(new Faker().commerce().productName())
//                .desc(new Faker().lorem().characters(255))
//                .price(new BigDecimal(new Faker().commerce().price()).doubleValue())
//                .currency("USD")
//                .visible(true)
//                .qty(new Faker().number().numberBetween(10, 30))
//                .size(new Faker().commerce().material())
//                .colour(new Faker().commerce().color())
//                .build();
//
//        MockMultipartFile[] files = {
//                new MockMultipartFile(
//                        "files",
//                        "uploads/image1.jpeg",
//                        MediaType.IMAGE_JPEG_VALUE,
//                        "Test image content".getBytes()
//                ),
//                new MockMultipartFile(
//                        "files",
//                        "uploads/image3.jpeg",
//                        MediaType.IMAGE_JPEG_VALUE,
//                        "Test image content".getBytes()
//                ),
//                new MockMultipartFile(
//                        "files",
//                        "image2.jpeg",
//                        MediaType.IMAGE_JPEG_VALUE,
//                        "Test image upload".getBytes()
//                )
//        };
//
//        // When, Then
//        this.MOCK_MVC
//                .perform(multipart(requestMapping)
//                        .file(files[0])
//                        .file(files[1])
//                        .file(files[2])
//                        .content(dto.toJson().toString())
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .with(csrf())
//                )
//                .andExpect(status().isCreated());
//    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void updateProduct() throws Exception {
        // Given
        long id = this.productRepository.findAll().get(0).getProductId();
        var dto = ProductDTO.builder()
                .id(id)
                .name("SEJU Development")
                .desc("Lorem 29")
                .price(Double.parseDouble(new Faker().commerce().price()))
                .build();

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void deleteProduct() throws Exception {
        long id = this.productRepository.findAll().get(0).getProductId();
        this.MOCK_MVC.perform(delete(requestMapping + "/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());
    }

}