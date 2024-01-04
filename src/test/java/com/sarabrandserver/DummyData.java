package com.sarabrandserver;

import com.github.javafaker.Faker;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.collection.dto.CollectionDTO;
import com.sarabrandserver.collection.service.WorkerCollectionService;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@TestConfiguration(proxyBeanMethods = false)
class DummyData {

    @Value(value = "${image.one}")
    private String image1;
    @Value(value = "${image.two}")
    private String image2;

    @Bean
    public CommandLineRunner runner(
            AuthService service,
            UserRepository repository,
            WorkerCategoryService categoryService,
            WorkerCollectionService collectionService,
            WorkerProductService productService
    ) {
        return args -> {
            String top = "top", bottom = "bottom";
            String summer = "summer 2023", fall = "fall 2023";

            categoryService.create(new CategoryDTO(top, true, ""));
            categoryService.create(new CategoryDTO(bottom, true, ""));
            collectionService.create(new CollectionDTO(summer, true));
            collectionService.create(new CollectionDTO(fall, true));

            for (int i = 0; i < 500; i++) {
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
                                collection,
                                new Faker().commerce().productName() + " " + i,
                                new SizeInventoryDTO[] {
                                        new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "medium"),
                                        new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "small"),
                                        new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "large")
                                },
                                new Faker().commerce().color() + " " + i
                        );

                var images = IMAGES();

                // Create products
                productService.create(data, images);
            }

            if (repository.findByPrincipal("admin@admin.com").isEmpty()) {
                var dto = new RegisterDTO(
                        "SEJU",
                        "Development",
                        "admin@admin.com",
                        "admin@admin.com",
                        "0000000000",
                        "password123"
                );
                service.workerRegister(dto);
            }
        };
    }

    private MockMultipartFile[] IMAGES() {
        return Arrays
                .stream(new Path[] { Paths.get(this.image1), Paths.get(this.image2) })
                .map(path -> {
                    try {
                        String originalFilename = path.getFileName().toString();
                        String contentType = Files.probeContentType(path);
                        return new MockMultipartFile(
                                "files",
                                originalFilename,
                                contentType,
                                Files.readAllBytes(path)
                        );
                    } catch (IOException e) {
                        return null;
                    }
                })
                .toArray(MockMultipartFile[]::new);
    }

}
