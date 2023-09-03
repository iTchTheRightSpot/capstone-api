package com.emmanuel.sarabrandserver.product.client;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.product.Result;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.emmanuel.sarabrandserver.product.worker.WorkerProductService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.emmanuel.sarabrandserver.product.ProductTestingData.getResult;
import static com.emmanuel.sarabrandserver.product.ProductTestingData.sizeInventoryDTOArray;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ClientProductControllerTest {
    private final static String requestMapping = "/api/v1/client/product";
    private final StringBuilder category = new StringBuilder();
    private final int detailSize = 1;
    private final StringBuilder productName = new StringBuilder();

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;

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
        // Create and save category
        this.category.append(new Faker().commerce().department());
        this.workerCategoryService.create(new CategoryDTO(this.category.toString(), true, ""));

        // Create and save Product
        String prodName = new Faker().commerce().productName();
        this.productName.append(prodName);
        SizeInventoryDTO[] sizeInventoryDTO1 = sizeInventoryDTOArray(this.detailSize);
        Result result = getResult(
                sizeInventoryDTO1,
                prodName,
                this.category.toString(),
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
    void databaseIsRunning() {
        assertTrue(container.isRunning());
        assertTrue(container.isCreated());
    }

    @Test
    @DisplayName(value = "Get ProductDetails for store front")
    void fetchProductDetails() throws Exception {
        var product = this.productRepository.findByProductName(this.productName.toString()).orElse(null);
        assertNotNull(product);
        this.MOCK_MVC
                .perform(get(requestMapping + "/detail").param("id", product.getUuid()))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));
    }
}