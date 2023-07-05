package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.clientz.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component @Profile(value = {"dev"})
@Slf4j
public class TestService {

    @Bean
    public CommandLineRunner commandLineRunner(
            ProductRepository prodRepo,
            CategoryRepository repo,
            WorkerCategoryService service,
            WorkerCollectionService serv,
            CollectionRepository collectionRepository,
            AuthService authService,
            ClientzRepository clientzRepository,
            ClientRoleRepo roleRepo
    ) {
        return args -> {
            roleRepo.deleteAll();
            clientzRepository.deleteAll();
            authService.workerRegister(new RegisterDTO(
                    "SEJU",
                    "Development",
                    "frank",
                    "frank",
                    "0000000000",
                    "password"
            ));
            categories(service, repo);
            collection(serv, collectionRepository);
            products(prodRepo);
        };
    }

    private void collection(WorkerCollectionService service, CollectionRepository repo) {
        repo.deleteAll();

        Set<String> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String str : set) {
            service.create(new CollectionDTO(str, true));
        }
    }

    private void categories(WorkerCategoryService service, CategoryRepository repo) {
        repo.deleteAll();

        Set<String> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String str : set) {
            service.create(new CategoryDTO(str, true, ""));
        }
    }

    private void products(ProductRepository repo) {
        repo.deleteAll();
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            set.add(new Faker().commerce().productName());
        }

        List<Product> list = new ArrayList<>();
        for (String str : set) {
            var product = Product.builder()
                    .name(str)
                    .defaultKey("")
                    .description(new Faker().lorem().characters(50))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency("USD")
                    .productDetails(new HashSet<>())
                    .build();

            for (int i = 0; i < 10; i++) {
                extracted(product);
            }
            list.add(product);
        }
        repo.saveAll(list);
    }

    private static void extracted(Product product) {
        var size = ProductSize.builder()
                .size(new Faker().commerce().material())
                .productDetails(new HashSet<>())
                .build();
        // ProductInventory
        var inventory = ProductInventory.builder()
                .quantity(new Faker().number().numberBetween(10, 40))
                .productDetails(new HashSet<>())
                .build();
        // ProductImage
        var image0 = ProductImage.builder()
                .imageKey(UUID.randomUUID().toString())
                .imagePath(new Faker().file().fileName())
                .build();

        var image1 = ProductImage.builder()
                .imageKey(UUID.randomUUID().toString())
                .imagePath(new Faker().file().fileName())
                .build();

        var image2 = ProductImage.builder()
                .imageKey(UUID.randomUUID().toString())
                .imagePath(new Faker().file().fileName())
                .build();

        // ProductColour
        var colour = ProductColour.builder()
                .colour(new Faker().color().name())
                .productDetails(new HashSet<>())
                .build();
        // ProductDetail
        var detail = ProductDetail.builder()
                .sku(UUID.randomUUID().toString())
                .isVisible(true)
                .createAt(new Date())
                .modifiedAt(null)
                .productImages(new HashSet<>())
                .build();

        detail.setProductSize(size);
        detail.setProductInventory(inventory);
        detail.setProductColour(colour);
        detail.addImages(image0);
        detail.addImages(image1);
        detail.addImages(image2);
        // Add detail to product
        product.addDetail(detail);
    }

}
