package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile(value = {"dev"})
@Slf4j
public class DummyDataDev {

    @Bean
    public CommandLineRunner runner(
            CategoryRepository categoryRepo,
            CollectionRepository collRepo,
            ProductRepository productRepo,
            ProductDetailRepo detailRepo
    ) {
        return args -> {
            categoryRepo.deleteAll();
            collRepo.deleteAll();
            detailRepo.deleteAll();
            productRepo.deleteAll();

//            Set<String> set = new HashSet<>();
//            for (int i = 0; i < 50; i++) {
//                set.add(new Faker().commerce().department());
//            }
//
//            for (String s : set) {
//                var category = ProductCategory.builder()
//                        .categoryName(s)
//                        .createAt(new Date())
//                        .isVisible(true)
//                        .productCategories(new HashSet<>())
//                        .build();
//                categoryRepo.save(category);
//            }
//
//            AtomicInteger g = new AtomicInteger(0);
//
//            categoryRepo.findAll().forEach(category -> {
//                var collection = ProductCollection.builder()
//                        .collection(category.getCategoryName() + (g.intValue() + 1))
//                        .createAt(new Date())
//                        .isVisible(true)
//                        .products(new HashSet<>())
//                        .build();
//
//                var saved = collRepo.save(collection);
//
//                String prodName = new Faker().commerce().productName() + (g.intValue() + 1);
//
//                var product = Product.builder()
//                        .productCategory(category)
//                        .productCollection(saved)
//                        .uuid(UUID.randomUUID().toString())
//                        .name(prodName)
//                        .defaultKey("default key")
//                        .description(new Faker().lorem().characters(50))
//                        .price(new BigDecimal(new Faker().commerce().price()))
//                        .currency("$")
//                        .productDetails(new HashSet<>())
//                        .build();
//
//                // ProductDetail
//                var detail = ProductDetail.builder()
//                        .sku(UUID.randomUUID().toString())
//                        .isVisible(true)
//                        .createAt(new Date())
//                        .modifiedAt(null)
//                        .productImages(new HashSet<>())
//                        .build();
//
//                // ProductSize
//                var size = ProductSize.builder()
//                        .size(String.valueOf(new Faker().number().randomDigitNotZero()))
//                        .productDetails(new HashSet<>())
//                        .build();
//                // ProductInventory
//                var inventory = ProductInventory.builder()
//                        .quantity(new Faker().number().numberBetween(10, 40))
//                        .productDetails(new HashSet<>())
//                        .build();
//
//                // ProductColour
//                var colour = ProductColour.builder()
//                        .colour(new Faker().color().name())
//                        .productDetails(new HashSet<>())
//                        .build();
//
//                detail.setProductSize(size);
//                detail.setProductInventory(inventory);
//                detail.setProductColour(colour);
//
//                for (int j = 0; j < 3; j++) {
//                    // ProductImage
//                    var image = ProductImage.builder()
//                            .imageKey(UUID.randomUUID().toString())
//                            .imagePath(new Faker().file().fileName())
//                            .build();
//                    detail.addImages(image);
//                }
//
//                // Add detail to product
//                product.addDetail(detail);
//
//                productRepo.save(product);
//            });
//
//            productRepo.findAll().forEach(product -> {
//                // ProductDetail
//                var detail = ProductDetail.builder()
//                        .sku(UUID.randomUUID().toString())
//                        .isVisible(true)
//                        .createAt(new Date())
//                        .modifiedAt(null)
//                        .product(product)
//                        .productImages(new HashSet<>())
//                        .build();
//
//                // ProductSize
//                var size = ProductSize.builder()
//                        .size(String.valueOf(new Faker().number().randomDigitNotZero()))
//                        .productDetails(new HashSet<>())
//                        .build();
//                // ProductInventory
//                var inventory = ProductInventory.builder()
//                        .quantity(new Faker().number().numberBetween(10, 40))
//                        .productDetails(new HashSet<>())
//                        .build();
//
//                // ProductColour
//                var colour = ProductColour.builder()
//                        .colour(new Faker().color().name())
//                        .productDetails(new HashSet<>())
//                        .build();
//
//                detail.setProductSize(size);
//                detail.setProductInventory(inventory);
//                detail.setProductColour(colour);
//
//                for (int j = 0; j < 3; j++) {
//                    // ProductImage
//                    var image = ProductImage.builder()
//                            .imageKey(UUID.randomUUID().toString())
//                            .imagePath(new Faker().file().fileName())
//                            .build();
//                    detail.addImages(image);
//                }
//
//                // Add detail to product
////                product.addDetail(detail);
//
//                detailRepo.save(detail);
//            });

        };
    }

}
