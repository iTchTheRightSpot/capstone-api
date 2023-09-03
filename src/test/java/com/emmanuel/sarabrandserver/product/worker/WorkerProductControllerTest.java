package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.Result;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.product.util.DetailDTO;
import com.emmanuel.sarabrandserver.product.util.ProductDTO;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.emmanuel.sarabrandserver.product.ProductTestingData.getResult;
import static com.emmanuel.sarabrandserver.product.ProductTestingData.sizeInventoryDTOArray;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class WorkerProductControllerTest {

    private final static String requestMapping = "/api/v1/worker/product";
    private final int detailSize = 10;
    private final StringBuilder category = new StringBuilder();
    private final StringBuilder colour = new StringBuilder();
    private final StringBuilder productName = new StringBuilder();

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductDetailRepo productDetailRepo;
    @Autowired private ProductSkuRepo productSkuRepo;

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
        this.category.append(new Faker().commerce().department());

        this.workerCategoryService.create(new CategoryDTO(this.category.toString(), true, ""));
        String prodName = new Faker().commerce().productName();
        this.productName.append(prodName);
        String colour = new Faker().commerce().color();
        this.colour.append(colour);

        // Product1 and ProductDetail1

        SizeInventoryDTO[] sizeInventoryDTO1 = sizeInventoryDTOArray(this.detailSize);
        Result result = getResult(
                sizeInventoryDTO1,
                prodName,
                this.category.toString(),
                colour
        );
        this.workerService.create(result.dto(), result.files());

        // Product2 and ProductDetail2
        SizeInventoryDTO[] sizeInventoryDTO2 = sizeInventoryDTOArray(1);
        Result result2 =
                getResult(
                        sizeInventoryDTO2,
                        new Faker().commerce().productName() + 2,
                        this.category.toString(),
                        colour
                );
        this.workerService.create(result2.dto(), result2.files());

        // Product3 and ProductDetail3
        SizeInventoryDTO[] sizeInventoryDTO3 = sizeInventoryDTOArray(2);
        Result result3 = getResult(
                sizeInventoryDTO3,
                new Faker().commerce().productName() + 3,
                this.category.toString(),
                colour
        );
        this.workerService.create(result3.dto(), result3.files());

        // Product4 and ProductDetail4
        SizeInventoryDTO[] sizeInventoryDTO4 = sizeInventoryDTOArray(5);
        var result4 = getResult(
                sizeInventoryDTO4,
                new Faker().commerce().productName() + 4,
                this.category.toString(),
                new Faker().commerce().color()
        );
        this.workerService.create(result4.dto(), result4.files());
    }

    @AfterEach
    void tearDown() {
        this.productRepository.deleteAll();
        this.categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Simulates fetching all Products")
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

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
            Simulates fetching ProductDetails by product uuid.
            Main objective is to validate native sql query
            """)
    void fetchAllDetail() throws Exception {
        // Given
        var product = this.productRepository.findByProductName(this.productName.toString()).orElse(null);
        assertNotNull(product);

        // Based on setUp
        this.MOCK_MVC
                .perform(get(requestMapping + "/detail").param("id", product.getUuid()))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Create a product")
    void create() throws Exception {
        // Given
        String[] sizeInventoryDTO = {
                SizeInventoryDTO.builder().size("small").qty(10).build().toJson().toString(),
                SizeInventoryDTO.builder().size("medium").qty(3).build().toJson().toString(),
                SizeInventoryDTO.builder().size("large").qty(15).build().toJson().toString(),
        };

        // Then
        this.MOCK_MVC
                .perform(multipart(requestMapping)
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image1.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image2.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .param("category", this.category.toString())
                        .param("collection", "")
                        .param("sizeInventory", sizeInventoryDTO[0])
                        .param("sizeInventory", sizeInventoryDTO[1])
                        .param("sizeInventory", sizeInventoryDTO[2])
                        .param("name", new Faker().commerce().productName())
                        .param("desc", new Faker().lorem().characters(255))
                        .param("price", new BigDecimal(new Faker().commerce().price()).toString())
                        .param("currency", "USD")
                        .param("visible", "true")
                        .param("colour", new Faker().commerce().color())
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
            Validate Custom Converter converts from String to SizeInventoryDTO[]
            considering only one entry for size and qty
            """)
    void val() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(multipart(requestMapping)
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image1.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image2.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .param("category", this.category.toString())
                        .param("collection", "")
                        .param("sizeInventory", SizeInventoryDTO.builder().size("small").qty(10).build().toJson().toString())
                        .param("name", new Faker().commerce().productName())
                        .param("desc", new Faker().lorem().characters(255))
                        .param("price", new BigDecimal(new Faker().commerce().price()).toString())
                        .param("currency", "USD")
                        .param("visible", "true")
                        .param("colour", new Faker().commerce().color())
                        .with(csrf())
                )
                .andExpect(status().isCreated());

    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
            Validates duplicate exception is thrown on creation of a new product.
            Exception is cause from duplicate product colour
            """)
    void ex() throws Exception {
        // Given
        String[] sizeInventoryDTO = {
                SizeInventoryDTO.builder().size("small").qty(10).build().toJson().toString(),
                SizeInventoryDTO.builder().size("medium").qty(3).build().toJson().toString(),
                SizeInventoryDTO.builder().size("large").qty(15).build().toJson().toString(),
        };

        // Then
        this.MOCK_MVC
                .perform(multipart(requestMapping)
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image1.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image2.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .param("category", this.category.toString())
                        .param("collection", "")
                        .param("sizeInventory", sizeInventoryDTO[0])
                        .param("sizeInventory", sizeInventoryDTO[1])
                        .param("sizeInventory", sizeInventoryDTO[2])
                        .param("name", this.productName.toString())
                        .param("desc", new Faker().lorem().characters(255))
                        .param("price", new BigDecimal(new Faker().commerce().price()).toString())
                        .param("currency", "USD")
                        .param("visible", "true")
                        .param("colour", this.colour.toString())
                        .with(csrf())
                )
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Validates bad request because sizeInventory JsonProperty is not present")
    void exThrown() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(multipart(requestMapping)
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image1.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image2.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .param("category", this.category.toString())
                        .param("collection", "")
                        .param("name", new Faker().commerce().productName())
                        .param("desc", new Faker().lorem().characters(255))
                        .param("price", new BigDecimal(new Faker().commerce().price()).toString())
                        .param("currency", "USD")
                        .param("visible", "true")
                        .param("colour", new Faker().commerce().color())
                        .with(csrf())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Validates Bad request due to sizeInventory[i] being null")
    void variableThrown() throws Exception {
        // Given
        String[] sizeInventoryDTO = {
                SizeInventoryDTO.builder().size("small").qty(10).build().toJson().toString(),
                null,
                SizeInventoryDTO.builder().size("large").qty(15).build().toJson().toString(),
        };

        // Then
        this.MOCK_MVC
                .perform(multipart(requestMapping)
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image1.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .file(new MockMultipartFile(
                                "files",
                                "uploads/image2.jpeg",
                                "image/jpeg",
                                "Test image content".getBytes()
                        ))
                        .param("category", this.category.toString())
                        .param("collection", "")
                        .param("sizeInventory", sizeInventoryDTO[0])
                        .param("sizeInventory", sizeInventoryDTO[1])
                        .param("sizeInventory", sizeInventoryDTO[2])
                        .param("name", new Faker().commerce().productName())
                        .param("desc", new Faker().lorem().characters(255))
                        .param("price", new BigDecimal(new Faker().commerce().price()).toString())
                        .param("currency", "USD")
                        .param("visible", "true")
                        .param("colour", new Faker().commerce().color())
                        .with(csrf())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Testing update a Product.")
    void updateProduct() throws Exception {
        // Given
        var product = this.productRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(product);
        var dto = ProductDTO.builder()
                .uuid(product.getUuid())
                .name("SEJU Development")
                .collection("")
                .category(category.toString())
                .desc(new Faker().lorem().characters(5, 200))
                .price(new BigDecimal(new Faker().commerce().price()))
                .build();

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Testing update a Product. Only difference is Product name is still the same")
    void update() throws Exception {
        // Given
        var product = this.productRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(product);

        var dto = ProductDTO.builder()
                .uuid(product.getUuid())
                .name(product.getName())
                .category(this.category.toString())
                .collection("")
                .desc(new Faker().lorem().characters(5, 200))
                .price(new BigDecimal(new Faker().commerce().price()))
                .build();

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Testing updating ProductDetail. Note ProductImage aren't updated")
    void updateDetail() throws Exception {
        var detail = this.productDetailRepo.findAll().get(0);
        var productSku = detail.getSkus().stream().findAny().orElse(null);

        assertNotNull(productSku);

        var dto = new DetailDTO(productSku.getSku(), true, 50, "large");

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping + "/detail")
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isOk());

        var findDetail = this.productSkuRepo.findBySku(productSku.getSku()).orElse(null);

        assertNotNull(findDetail);

        assertEquals(dto.getSku(), findDetail.getSku());
        assertEquals(dto.getQty(), findDetail.getInventory());
        assertEquals(dto.getSize(), findDetail.getSize());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteProduct() throws Exception {
        var product = this.productRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(product);
        this.MOCK_MVC.perform(delete(requestMapping).param("id", product.getUuid()).with(csrf()))
                .andExpect(status().isNoContent());
        var del = this.productRepository.findById(product.getProductId()).orElse(null);
        assertNull(del);
    }

}