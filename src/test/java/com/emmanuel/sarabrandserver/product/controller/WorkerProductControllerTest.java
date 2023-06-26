package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.product.dto.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.worker.WorkerProductService;
import com.github.javafaker.Faker;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
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
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class WorkerProductControllerTest {
    private final static String requestMapping = "/api/v1/worker/category";

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;

    @Container private static final MySQLContainer<?> container;
    @Container private static final RedisContainer redis;

    static {
        container = new MySQLContainer<>("mysql:latest")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");

        redis = new RedisContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);
    }

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        Set<String> set = new HashSet<>();

        for (int i = 0; i < 20; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String str : set) {
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

            // Validate deleting
            var dto1 = CreateProductDTO.builder()
                    .category(str)
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

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void fetchAll() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(get(requestMapping).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void create() throws Exception {
        // Given
        var dto = CreateProductDTO.builder()
                .category(new Faker().commerce().department())
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
                        "files",
                        "uploads/image1.jpeg",
                        "image/jpeg",
                        "Test image content".getBytes()
                ),
                new MockMultipartFile(
                        "files",
                        "uploads/image3.jpeg",
                        "image/jpeg",
                        "Test image content".getBytes()
                ),
                new MockMultipartFile(
                        "file",
                        "image2.jpeg",
                        "image/jpeg",
                        "Test image upload".getBytes()
                )
        };

        // When, Then
        this.MOCK_MVC
                .perform(multipart(HttpMethod.POST, requestMapping)
                        .file(files[0])
                        .file(files[1])
                        .file(files[2])
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

//    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
//    void update() { }
//
//    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
//    void deleteAllProduct() throws Exception { }

    /* Validate only product and sku are deleted not products with the same name. */
    @Test @WithMockUser(username = "admin@admin.com", password = "password", authorities = {"WORKER"})
    void deleteAProduct() throws Exception {
        String name = "custom-product";

        // Always present because of @BeforeEach
        var product = this.productRepository.findByProductName(name).get();
        String sku = product.getProductDetails().stream().findFirst().get().getSku().trim();
        int count = product.getProductDetails().size();

        this.MOCK_MVC
                .perform(delete(requestMapping + "/{name}/{sku}", name, sku).with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        var sub = this.productRepository.findByProductName(name).get().getProductDetails().size();

        assertNotEquals(count, sub);
    }

}