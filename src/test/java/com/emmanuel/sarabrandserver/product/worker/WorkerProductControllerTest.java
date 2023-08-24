package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.util.DetailDTO;
import com.emmanuel.sarabrandserver.product.util.ProductDTO;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Slf4j
class WorkerProductControllerTest {

    private final static String requestMapping = "/api/v1/worker/product";
    private final StringBuilder category = new StringBuilder();
    private final StringBuilder colour = new StringBuilder();
    private final StringBuilder productName = new StringBuilder();

    @Autowired
    private MockMvc MOCK_MVC;
    @Autowired
    private WorkerProductService workerService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WorkerCategoryService workerCategoryService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductDetailRepo productDetailRepo;

    @Container
    private static final MySQLContainer<?> container;

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

        for (int i = 0; i < 5; i++) {
            set.add(new Faker().commerce().department());
        }

        // Save Category
        for (String str : set) {
            if (this.category.isEmpty()) {
                this.category.append(str);
            }
            this.workerCategoryService.create(new CategoryDTO(str, true, ""));
        }

        // Save Products
        int i = 0;
        for (String str : set) {
            SizeInventoryDTO[] sizeInventoryDTO = {
                    SizeInventoryDTO.builder().size("small").qty(10).build(),
                    SizeInventoryDTO.builder().size("medium").qty(3).build(),
                    SizeInventoryDTO.builder().size("large").qty(15).build(),
            };

            String c = new Faker().commerce().color() + i;

            if (this.colour.isEmpty()) {
                this.colour.append(c);
            }

            String name = new Faker().commerce().productName();

            if (this.productName.isEmpty()) {
                this.productName.append(name);
            }

            var dto = CreateProductDTO.builder()
                    .category(str)
                    .collection("")
                    .name(name)
                    .desc(new Faker().lorem().characters(255))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency("USD")
                    .visible(true)
                    .colour(c)
                    .build();

            MockMultipartFile[] files = {
                    new MockMultipartFile(
                            "file",
                            "uploads/image1.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            "uploads/image2.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            "uploads/image3.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
            };

            this.workerService.create(dto, sizeInventoryDTO, files);

            i += 1;
        }
    }

    @AfterEach
    void tearDown() {
        this.productRepository.deleteAll();
        this.categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Fetch all Products")
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
    @DisplayName(value = "Fetch ProductDetails")
    void fetchAllDetail() throws Exception {
        this.MOCK_MVC
                .perform(get(requestMapping + "/{name}", "custom-product")
                        .param("page", "0")
                        .param("size", "50")
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Testing update a Product.")
    void updateProduct() throws Exception {
        // Given
        String id = this.productRepository.findAll().get(0).getUuid();
        var dto = ProductDTO.builder()
                .uuid(id)
                .name("SEJU Development")
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
        var product = this.productRepository.findAll().get(1);
        var dto = ProductDTO.builder()
                .uuid(product.getUuid())
                .name(product.getName())
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

        var dto = new DetailDTO(detail.getSku(), true, 50, "large");

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping + "/detail")
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isOk());

        var findDetail = this.productDetailRepo.findById(detail.getProductDetailId());

        assertDoesNotThrow(findDetail::get);

        assertEquals(dto.getSku(), findDetail.get().getSku());
        assertEquals(dto.getQty(), findDetail.get().getSizeInventory().getInventory());
        assertEquals(dto.getSize(), findDetail.get().getSizeInventory().getSize());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteProduct() throws Exception {
        this.MOCK_MVC.perform(delete(requestMapping + "/{name}", this.productName).with(csrf()))
                .andExpect(status().isNoContent());
    }

}