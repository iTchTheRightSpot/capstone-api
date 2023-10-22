package com.sarabrandserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.collection.dto.CollectionDTO;
import com.sarabrandserver.collection.repository.CollectionRepository;
import com.sarabrandserver.collection.service.WorkerCollectionService;
import com.sarabrandserver.data.Result;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc MOCKMVC;
    @Autowired protected ObjectMapper MAPPER;

    protected int detailSize = 10;

    @Autowired protected WorkerProductService workerProductService;
    @Autowired protected ProductRepo productRepo;
    @Autowired protected ProductSkuRepo productSkuRepo;
    @Autowired protected ProductDetailRepo productDetailRepo;
    @Autowired protected WorkerCategoryService workerCategoryService;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected WorkerCollectionService collectionService;
    @Autowired protected CollectionRepository collectionRepository;

    @BeforeEach
    void setUp() {
        // Persist collection
        this.collectionService
                .create(new CollectionDTO(new Faker().commerce().department(), false));

        this.collectionService
                .create(new CollectionDTO(new Faker().commerce().department() + 1, false));

        this.collectionService
                .create(new CollectionDTO(new Faker().commerce().department() + 2, true));

        // Persist category
        String category = new Faker().commerce().department();

        this.workerCategoryService.create(new CategoryDTO(category, true, ""));
        this.workerCategoryService
                .create(new CategoryDTO(new Faker().commerce().department() + 1, true, ""));
        this.workerCategoryService
                .create(new CategoryDTO(new Faker().commerce().department() + 2, true, ""));

        // colour
        String colour = new Faker().commerce().color();

        SizeInventoryDTO[] sizeInventoryDTO1 = TestingData.sizeInventoryDTOArray(detailSize);
        Result result = TestingData.getResult(
                sizeInventoryDTO1,
                new Faker().commerce().productName(),
                category,
                colour
        );
        this.workerProductService.create(result.dto(), result.files());

        // Product2 and ProductDetail2
        SizeInventoryDTO[] sizeInventoryDTO2 = TestingData.sizeInventoryDTOArray(1);
        Result result2 =
                TestingData.getResult(
                        sizeInventoryDTO2,
                        new Faker().commerce().productName() + 2,
                        category,
                        colour
                );
        this.workerProductService.create(result2.dto(), result2.files());

        // Product3 and ProductDetail3
        SizeInventoryDTO[] sizeInventoryDTO3 = TestingData.sizeInventoryDTOArray(2);
        Result result3 = TestingData.getResult(
                sizeInventoryDTO3,
                new Faker().commerce().productName() + 3,
                category,
                colour
        );
        this.workerProductService.create(result3.dto(), result3.files());

        // Product4 and ProductDetail4
        SizeInventoryDTO[] sizeInventoryDTO4 = TestingData.sizeInventoryDTOArray(5);
        var result4 = TestingData.getResult(
                sizeInventoryDTO4,
                new Faker().commerce().productName() + 4,
                category,
                new Faker().commerce().color()
        );
        this.workerProductService.create(result4.dto(), result4.files());
    }

    @AfterEach
    void tearDown() {
        this.productSkuRepo.deleteAll();
        this.productRepo.deleteAll();
        this.categoryRepository.deleteAll();
        this.collectionRepository.deleteAll();
    }

}