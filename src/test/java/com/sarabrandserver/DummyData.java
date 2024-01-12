package com.sarabrandserver;

import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;

@TestConfiguration(proxyBeanMethods = false)
class DummyData {

    @Bean
    public CommandLineRunner runner(
            AuthService authService,
            UserRepository repository,
            CategoryRepository categoryRepository,
            WorkerProductService workerProductService
    ) {
        return args -> {
            var category = categoryRepository
                    .save(
                            ProductCategory.builder()
                                    .name("category")
                                    .isVisible(true)
                                    .parentCategory(null)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );

            TestingData.dummyProducts(category, 2, workerProductService);

            var clothes = categoryRepository
                    .save(
                            ProductCategory.builder()
                                    .name("clothes")
                                    .isVisible(true)
                                    .parentCategory(category)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );

            TestingData.dummyProducts(clothes, 5, workerProductService);

            var shirt = categoryRepository
                    .save(
                            ProductCategory.builder()
                                    .name("t-shirt")
                                    .isVisible(true)
                                    .parentCategory(clothes)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );

            TestingData.dummyProducts(shirt, 10, workerProductService);

            var furniture = categoryRepository
                    .save(
                            ProductCategory.builder()
                                    .name("furniture")
                                    .isVisible(true)
                                    .parentCategory(category)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );

            TestingData.dummyProducts(furniture, 3, workerProductService);

            var collection = categoryRepository
                    .save(
                            ProductCategory.builder()
                                    .name("collection")
                                    .isVisible(true)
                                    .parentCategory(null)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );

            TestingData.dummyProducts(collection, 1, workerProductService);

            var winter = categoryRepository
                    .save(
                            ProductCategory.builder()
                                    .name("winter 2024")
                                    .isVisible(true)
                                    .parentCategory(collection)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );

            TestingData.dummyProducts(winter, 15, workerProductService);

            if (repository.findByPrincipal("admin@admin.com").isEmpty()) {
                var dto = new RegisterDTO(
                        "SEJU",
                        "Development",
                        "admin@admin.com",
                        "",
                        "0000000000",
                        "password123"
                );
                authService.workerRegister(dto);
            }

        };
    }

}
