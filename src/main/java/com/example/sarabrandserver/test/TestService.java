package com.example.sarabrandserver.test;

import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import com.example.sarabrandserver.collection.entity.ProductCollection;
import com.example.sarabrandserver.collection.repository.CollectionRepository;
import com.example.sarabrandserver.product.entity.*;
import com.example.sarabrandserver.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component @Slf4j
public class TestService {

    private record Pojo(
            String name,
            String des,
            BigDecimal price,
            String curr,
            String sku,
            boolean status,
            String size,
            int quantity,
            String image,
            String colour
    ) {}

    private record CatPojo() {}

    @Bean
    public CommandLineRunner commandLineRunner(CategoryRepository repo, CollectionRepository collectionRepository) {
        return args -> {
            categories(repo);
            collection(collectionRepository);
        };
    }

    private void collection(CollectionRepository repo) {
        repo.deleteAll();

        Set<String> set = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String str : set) {
            var collection = ProductCollection.builder()
                    .collection(str)
                    .createAt(new Date())
                    .modifiedAt(null)
                    .isVisible(true)
                    .products(new HashSet<>())
                    .build();
            repo.save(collection);
        }
    }

    private void categories(CategoryRepository repo) {
        repo.deleteAll();

        Set<String> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String str : set) {
            var category = ProductCategory.builder()
                    .categoryName(str)
                    .createAt(new Date())
                    .modifiedAt(null)
                    .isVisible(true)
                    .productCategories(new HashSet<>())
                    .product(new HashSet<>())
                    .build();

            for (int j = 0; j < 3; j++) {
                var sub = ProductCategory.builder()
                        .categoryName(new Faker().name().firstName())
                        .createAt(new Date())
                        .modifiedAt(null)
                        .isVisible(true)
                        .productCategories(new HashSet<>())
                        .product(new HashSet<>())
                        .build();
                category.addCategory(sub);
            }
            repo.save(category);
        }
//        repo.fetchCategories()
//                .forEach(e -> log.info("Parent Category name {} and children {}", e.getCategory(), e.getSub()));
    }

    private void products(ProductRepository repo) {
        repo.deleteAll();

        Set<String> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            set.add(new Faker().commerce().productName());
        }

        for (String str : set) {
            var product = Product.builder()
                    .name(str)
                    .description(new Faker().lorem().characters(50))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency(new Faker().currency().name())
                    .defaultImageKey(new Faker().file().fileName())
                    .productDetails(new HashSet<>())
                    .build();

            for (int j = 0; j < 3; j++) {
                // ProductSize
                var size = ProductSize.builder()
                        .size(new Faker().numerify(String.valueOf(j)))
                        .productDetails(new HashSet<>())
                        .build();
                // ProductInventory
                var inventory = ProductInventory.builder()
                        .quantity(new Faker().number().numberBetween(10, 40))
                        .productDetails(new HashSet<>())
                        .build();
                // ProductImage
                var image = ProductImage.builder()
                        .imageKey(UUID.randomUUID().toString())
                        .imagePath(new Faker().file().fileName())
                        .productDetails(new HashSet<>())
                        .build();
                // ProductColour
                var colour = ProductColour.builder()
                        .colour(new Faker().color().name())
                        .productDetails(new HashSet<>())
                        .build();
                // ProductDetail
                var detail = ProductDetail.builder()
                        .sku(UUID.randomUUID().toString())
                        .isVisible(false)
                        .createAt(new Date())
                        .modifiedAt(null)
                        .build();
                detail.setProductSize(size);
                detail.setProductInventory(inventory);
                detail.setProductImage(image);
                detail.setProductColour(colour);
                // Add detail to product
                product.addDetails(detail);
            }
            repo.save(product);
        }

        repo.fetchAll(PageRequest.of(0, 100)).forEach(e -> {
            var pojo = new Pojo(e.getName(), e.getDesc(), e.getPrice(), e.getCurrency(), e.getSku(), e.getStatus(),
                    e.getSizes(), e.getQuantity(), e.getImage(), e.getColour());
            log.info("Pojo {}", pojo);
        });

    }

    private void test(TestEntityRepo repo) {
        repo.deleteAll();

        Set<String> set = new HashSet<>();

        for (int i = 0; i < 8; i++) {
            set.add(new Faker().commerce().productName());
        }

        for (String str : set) {
            var test = TestEntity.builder()
                    .name(str)
                    .sku(UUID.randomUUID().toString())
                    .build();


            for (int i = 0; i < 2; i++) {
                var child = TestChildEntity.builder()
                        .name(new Faker().commerce().color())
                        .entities(new HashSet<>())
                        .build();
                test.setTestChildEntity(child);
            }
            repo.save(test);
        }

        repo.findAll().forEach(e -> {
            log.info("Name {}", e.getName());
            log.info("SKU {}", e.getSku());
            log.info("Child Entities {}", e.getTestChildEntity());
        });

    }

}
