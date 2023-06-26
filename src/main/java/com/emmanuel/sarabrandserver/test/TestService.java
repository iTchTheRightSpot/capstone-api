package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.worker.WorkerProductService;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component @Slf4j
public class TestService {

    @Bean
    public CommandLineRunner commandLineRunner(
            ProductRepository prodRepo,
            CategoryRepository repo,
            CollectionRepository collectionRepository
    ) {
        return args -> {
            categories(repo);
            collection(collectionRepository);
            products(prodRepo);
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
    }

    private void products(WorkerProductService service, ProductRepository repo) {
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
                    .currency("USD")
                    .productDetails(new HashSet<>())
                    .build();
        }

//        repo.list().forEach(pojo -> {
//            String str = """
//                    name %s, desc %s, price %s, curr %s
//
//                    Detail %s
//                    """.formatted(pojo.getName(), pojo.getDesc(), pojo.getPrice(), pojo.getCurrency(), pojo.getDetail());
//
//            log.info(str);
//        });

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
