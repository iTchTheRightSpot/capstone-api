package com.sarabrandserver.product.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.collection.dto.CollectionDTO;
import com.sarabrandserver.collection.repository.CollectionRepository;
import com.sarabrandserver.collection.service.WorkerCollectionService;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.product.repository.ProductRepository;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.product.util.SizeInventoryDTO;
import com.sarabrandserver.product.util.UpdateProductDTO;
import com.sarabrandserver.util.Result;
import com.github.javafaker.Faker;
import com.sarabrandserver.util.TestingData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkerProductControllerTest extends AbstractIntegrationTest {

    private final static String requestMapping = "/api/v1/worker/product";
    private final StringBuilder category = new StringBuilder();
    private final StringBuilder colour = new StringBuilder();
    private final StringBuilder productName = new StringBuilder();

    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private WorkerCollectionService collectionService;
    @Autowired private CollectionRepository collectionRepository;

    @BeforeEach
    void setUp() {
        // Persist collection
        this.collectionService.create(new CollectionDTO(new Faker().commerce().department(), false));

        // Persist category
        this.category.append(new Faker().commerce().department());
        this.workerCategoryService.create(new CategoryDTO(this.category.toString(), true, ""));

        String prodName = new Faker().commerce().productName();
        this.productName.append(prodName);
        String colour = new Faker().commerce().color();
        this.colour.append(colour);

        // Product1 and ProductDetail1
        int detailSize = 10;
        SizeInventoryDTO[] sizeInventoryDTO1 = TestingData.sizeInventoryDTOArray(detailSize);
        Result result = TestingData.getResult(
                sizeInventoryDTO1,
                prodName,
                this.category.toString(),
                colour
        );
        this.workerService.create(result.dto(), result.files());

        // Product2 and ProductDetail2
        SizeInventoryDTO[] sizeInventoryDTO2 = TestingData.sizeInventoryDTOArray(1);
        Result result2 =
                TestingData.getResult(
                        sizeInventoryDTO2,
                        new Faker().commerce().productName() + 2,
                        this.category.toString(),
                        colour
                );
        this.workerService.create(result2.dto(), result2.files());

        // Product3 and ProductDetail3
        SizeInventoryDTO[] sizeInventoryDTO3 = TestingData.sizeInventoryDTOArray(2);
        Result result3 = TestingData.getResult(
                sizeInventoryDTO3,
                new Faker().commerce().productName() + 3,
                this.category.toString(),
                colour
        );
        this.workerService.create(result3.dto(), result3.files());

        // Product4 and ProductDetail4
        SizeInventoryDTO[] sizeInventoryDTO4 = TestingData.sizeInventoryDTOArray(5);
        var result4 = TestingData.getResult(
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
        this.collectionRepository.deleteAll();
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
    @DisplayName(value = "Create a product")
    void create() throws Exception {
        // Given
        SizeInventoryDTO[] dtos = {
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
                        .param("sizeInventory", this.MAPPER.writeValueAsString(dtos[0]))
                        .param("sizeInventory", this.MAPPER.writeValueAsString(dtos[1]))
                        .param("sizeInventory", this.MAPPER.writeValueAsString(dtos[2]))
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
    @DisplayName(value = "Update Product. Ex thrown because product name exists")
    void updateEx() throws Exception {
        // Given
        var product = this.productRepository.findAll();
        assertFalse(product.isEmpty());
        assertTrue(product.size() > 2);

        var category = this.categoryRepository.findAll();
        assertFalse(category.isEmpty());

        // Payload
        var dto = UpdateProductDTO.builder()
                .category(category.get(0).getCategoryName())
                .categoryId(category.get(0).getUuid())
                .collection("")
                .collectionId("")
                .uuid(product.get(0).getUuid())
                .name(product.get(1).getName())
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
                .andExpect(status().isConflict())
                .andDo(result -> assertTrue(result.getResolvedException() instanceof DuplicateException));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Update Product. Category and Collection are in the payload")
    void updateProduct() throws Exception {
        // Given
        var product = this.productRepository.findAll();
        assertFalse(product.isEmpty());

        var category = this.categoryRepository.findAll();
        assertFalse(category.isEmpty());

        var collection = this.collectionRepository.findAll();
        assertFalse(collection.isEmpty());

        // Payload
        var dto = UpdateProductDTO.builder()
                .category(category.get(0).getCategoryName())
                .categoryId(category.get(0).getUuid())
                .collection(collection.get(0).getCollection())
                .collectionId(collection.get(0).getUuid())
                .uuid(product.get(0).getUuid())
                .name("SEJU Development")
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
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Update Product. Collection and collection_id are empty payload")
    void updateCol() throws Exception {
        // Given
        var product = this.productRepository.findAll();
        assertFalse(product.isEmpty());

        var category = this.categoryRepository.findAll();
        assertFalse(category.isEmpty());

        // payload
        var dto = UpdateProductDTO.builder()
                .category(category.get(0).getCategoryName())
                .categoryId(category.get(0).getUuid())
                .collection("")
                .collectionId("")
                .uuid(product.get(0).getUuid())
                .name("SEJU Development")
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
                .andExpect(status().isNoContent());
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

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = "Delete Product. Exception thrown because product has more than 1 ProductDetail ")
    void deleteProductEx() throws Exception {
        var product = this.productRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(product);

        this.MOCKMVC.perform(delete(requestMapping).param("id", product.getUuid()).with(csrf()))
                .andExpect(status().isNoContent());

        var del = this.productRepository.findById(product.getProductId()).orElse(null);
        assertNull(del);
    }

}