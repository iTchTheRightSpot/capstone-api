package com.sarabrandserver;

import com.github.javafaker.Faker;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@TestConfiguration(proxyBeanMethods = false)
class DummyData {

    @Bean
    public CommandLineRunner runner(
            AuthService service,
            UserRepository repository,
            WorkerCategoryService categoryService,
            CategoryRepository categoryRepository,
            WorkerProductService productService
    ) {
        return args -> {
            String top = "top", bottom = "bottom";
            String summer = "summer 2023", fall = "fall 2023";

//            categoryService.create(new CategoryDTO(top, true, ""));
//            categoryService.create(new CategoryDTO(bottom, true, ""));
//            collectionService.create(new CollectionDTO(summer, true));
//            collectionService.create(new CollectionDTO(fall, true));
//
//            dummyProducts(top, summer, bottom, fall, productService);
//
//            if (repository.findByPrincipal("admin@admin.com").isEmpty()) {
//                var dto = new RegisterDTO(
//                        "SEJU",
//                        "Development",
//                        "admin@admin.com",
//                        "admin@admin.com",
//                        "0000000000",
//                        "password123"
//                );
//                service.workerRegister(dto);
//            }
//
//            dummySubCategories(categoryService, categoryRepository);
        };
    }

    final void dummyProducts(
            String top,
            String summer,
            String bottom,
            String fall,
            WorkerProductService service
    ) {
        for (int i = 0; i < 50; i++) {
            String category;
            String collection;

            if (i % 2 == 0) {
                category = top;
                collection = summer;
            } else {
                category = bottom;
                collection = fall;
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
            service.create(data, images);
        }
    }

    final void dummySubCategories(WorkerCategoryService service, CategoryRepository repository) {
        List<ProductCategory> categories = repository.findAll();

        String[] names = new String[categories.size()];

        for (int i = 0; i < categories.size(); i++) {
            String name = new Faker().commerce().department() + i;
            var dto = new CategoryDTO(
                    name,
                    true,
                    categories.get(i).getName()
            );

            names[i] = name;
            service.create(dto);
        }

        for (int i = 0; i < names.length; i++) {
            ProductCategory category = service.findByName(names[i]);
            for (int j = 0; j < 3; j++) {
                service.create(
                        new CategoryDTO(
                                new Faker().commerce().department() + ((i + 1) * categories.size()),
                                true,
                                category.getName()
                        )
                );
            }
        }

    }

}
