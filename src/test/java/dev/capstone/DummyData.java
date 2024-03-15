package dev.capstone;

import dev.capstone.auth.dto.RegisterDto;
import dev.capstone.auth.service.AuthService;
import dev.capstone.category.dto.CategoryDTO;
import dev.capstone.category.entity.ProductCategory;
import dev.capstone.category.service.WorkerCategoryService;
import dev.capstone.data.TestData;
import dev.capstone.enumeration.RoleEnum;
import dev.capstone.product.service.WorkerProductService;
import dev.capstone.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class DummyData {

    @Bean
    public CommandLineRunner runner(
            AuthService authService,
            UserRepository repository,
            WorkerCategoryService catService,
            WorkerProductService workerProductService
    ) {
        return args -> {
            extracted(catService, workerProductService);

            if (repository.userByPrincipal("admin@admin.com").isEmpty()) {
                var dto = new RegisterDto(
                        "SEJU",
                        "Development",
                        "admin@admin.com",
                        "",
                        "0000000000",
                        "password123"
                );
                authService.register(null, dto, RoleEnum.WORKER);
            }
        };
    }

    private static void extracted(WorkerCategoryService catService, WorkerProductService service) {
        var category = ProductCategory.builder()
                .categoryId(1L)
                .build();
        catService.create(new CategoryDTO("category", true, null));
        TestData.dummyProducts(category, 2, service);

        var clothes = ProductCategory.builder()
                .categoryId(2L)
                .build();
        catService.create(new CategoryDTO("clothes", true, 1L));
        TestData.dummyProducts(clothes, 5, service);

        var shirt = ProductCategory.builder()
                .categoryId(3L)
                .build();
        catService.create(new CategoryDTO("t-shirt", true, 2L));
        TestData.dummyProducts(shirt, 10, service);

        var furniture = ProductCategory.builder()
                .categoryId(4L)
                .build();
        catService.create(new CategoryDTO("furniture", true, null));
        TestData.dummyProducts(furniture, 3, service);

        var collection = ProductCategory.builder()
                .categoryId(5L)
                .build();
        catService.create(new CategoryDTO("collection", true, null));
        TestData.dummyProducts(collection, 1, service);

        var winter = ProductCategory.builder()
                .categoryId(6L)
                .build();
        catService.create(new CategoryDTO("winter 2024", true, 5L));
        TestData.dummyProducts(winter, 15, service);
    }

}