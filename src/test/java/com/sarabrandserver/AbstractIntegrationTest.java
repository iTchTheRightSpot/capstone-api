package com.sarabrandserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.service.WorkerCategoryService;
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

    protected int detailSize = 3;

    @Autowired protected WorkerProductService workerProductService;
    @Autowired protected ProductRepo productRepo;
    @Autowired protected ProductSkuRepo productSkuRepo;
    @Autowired protected ProductDetailRepo productDetailRepo;
    @Autowired protected WorkerCategoryService workerCategoryService;
    @Autowired protected CategoryRepository categoryRepository;

    // persist 5 products
    @BeforeEach
    void beforeEachMethod() {
        String cat1 = new Faker().commerce().department() + 1;
        String cat2 = new Faker().commerce().department() + 2;

        // Persist collection
        this.workerCategoryService.create(new CategoryDTO(cat1, true, ""));
        this.workerCategoryService.create(new CategoryDTO(cat2, true, ""));

        for (int i = 0; i < 5; i++) {
            String category;

            if (i % 2 == 0) {
                category = cat1;
            } else {
                category = cat2;
            }

            var data = TestingData
                    .productDTO(
                            category,
                            new Faker().commerce().productName() + " " + i,
                            new SizeInventoryDTO[] {
                                    new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "medium"),
                                    new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "small"),
                                    new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "large")
                            },
                            new Faker().commerce().color() + " " + i
                    );

            var images = TestingData.files();

            // Create product
            workerProductService.create(data, images);
        }
    }

    @AfterEach
    void afterEachMethod() {
        this.productSkuRepo.deleteAll();
        this.productRepo.deleteAll();
        this.categoryRepository.deleteAll();
    }

}