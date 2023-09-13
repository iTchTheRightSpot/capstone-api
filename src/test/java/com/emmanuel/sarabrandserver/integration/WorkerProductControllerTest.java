package com.emmanuel.sarabrandserver.integration;

import com.emmanuel.sarabrandserver.AbstractIntegrationTest;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.util.Result;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.product.util.DetailDTO;
import com.emmanuel.sarabrandserver.product.util.ProductDTO;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.emmanuel.sarabrandserver.product.worker.WorkerProductService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static com.emmanuel.sarabrandserver.util.TestingData.getResult;
import static com.emmanuel.sarabrandserver.util.TestingData.sizeInventoryDTOArray;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkerProductControllerTest extends AbstractIntegrationTest {

    private final static String requestMapping = "/api/v1/worker/product";
    private final int detailSize = 10;
    private final StringBuilder category = new StringBuilder();
    private final StringBuilder colour = new StringBuilder();
    private final StringBuilder productName = new StringBuilder();

    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductDetailRepo productDetailRepo;
    @Autowired private ProductSkuRepo productSkuRepo;

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
        this.MOCKMVC
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
        this.MOCKMVC
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
        SizeInventoryDTO[] sizeInventoryDTO = {
                SizeInventoryDTO.builder().size("small").qty(10).build(),
                SizeInventoryDTO.builder().size("medium").qty(3).build(),
                SizeInventoryDTO.builder().size("large").qty(15).build(),
        };

        // Then
        this.MOCKMVC
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
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[0]))
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[1]))
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[2]))
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
        String sizeInv = this.MAPPER
                .writeValueAsString(SizeInventoryDTO.builder().size("small").qty(10).build());
        this.MOCKMVC
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
                        .param("sizeInventory", sizeInv)
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
        SizeInventoryDTO[] sizeInventoryDTO = {
                SizeInventoryDTO.builder().size("small").qty(10).build(),
                SizeInventoryDTO.builder().size("medium").qty(3).build(),
                SizeInventoryDTO.builder().size("large").qty(15).build(),
        };

        // Then
        this.MOCKMVC
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
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[0]))
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[1]))
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[2]))
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
        this.MOCKMVC
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
        SizeInventoryDTO[] sizeInventoryDTO = {
                SizeInventoryDTO.builder().size("small").qty(10).build(),
                SizeInventoryDTO.builder().size("large").qty(15).build(),
        };

        // Then
        this.MOCKMVC
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
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[0]))
                        .param("sizeInventory", (String) null)
                        .param("sizeInventory", this.MAPPER.writeValueAsString(sizeInventoryDTO[1]))
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
        this.MOCKMVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
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
        this.MOCKMVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
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
        this.MOCKMVC
                .perform(put(requestMapping + "/detail")
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
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

        this.MOCKMVC.perform(delete(requestMapping).param("id", product.getUuid()).with(csrf()))
                .andExpect(status().isNoContent());

        var del = this.productRepository.findById(product.getProductId()).orElse(null);
        assertNull(del);
    }

}