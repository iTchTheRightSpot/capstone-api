package dev.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.security.controller.RegisterDto;
import dev.webserver.security.controller.AuthenticationService;
import dev.webserver.category.CategoryDTO;
import dev.webserver.category.ProductCategory;
import dev.webserver.category.CategoryRepository;
import dev.webserver.category.WorkerCategoryService;
import dev.webserver.data.TestData;
import dev.webserver.enumeration.RoleEnum;
import dev.webserver.product.WorkerProductService;
import dev.webserver.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

class ApplicationTest {

    public static void main(String... args) {
        SpringApplication
                .from(Application::main)
                .with(TestConfig.class, TestController.class, DummyData.class)
                .run(args);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        static final Logger log = LoggerFactory.getLogger(TestConfig.class);

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        @ServiceConnection
        // @RestartScope
        static MySQLContainer<?> mySQLContainer() {
            try (var sql = new MySQLContainer<>("mysql:8.0")) {
                return sql.withDatabaseName("capstone_db")
                        .withUsername("capstone")
                        .withPassword("capstone");
            } catch (RuntimeException ex) {
                log.error("failed to start up MySQL in test/dev mode");
                throw new RuntimeException();
            }
        }

    }

    @TestConfiguration(proxyBeanMethods = false)
    static class DummyData {

        @Bean
        public CommandLineRunner runner(
                AuthenticationService authenticationService,
                UserRepository repository,
                WorkerCategoryService catService,
                WorkerProductService workerProductService,
                CategoryRepository categoryRepository
        ) {
            return args -> {

                if (categoryRepository.findByName("category").isEmpty()) {
                    extracted(catService, workerProductService);
                }

                if (repository.userByPrincipal("admin@admin.com").isEmpty()) {
                    var dto = new RegisterDto(
                            "SEJU",
                            "Development",
                            "admin@admin.com",
                            "",
                            "0000000000",
                            "password123"
                    );
                    authenticationService.register(null, dto, RoleEnum.WORKER);
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

}