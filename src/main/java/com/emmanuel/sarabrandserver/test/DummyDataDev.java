package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@Profile(value = {"dev"})
public class DummyDataDev {

    @Bean
    public CommandLineRunner runner(CategoryRepository categoryRepo, ProductRepository productRepo, ProductDetailRepo detailRepo) {
        return args -> {
            categoryRepo.deleteAll();
            detailRepo.deleteAll();
            productRepo.deleteAll();

            Set<String> set = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                set.add(new Faker().commerce().department());
            }

            for (String s : set) {
                var category = ProductCategory.builder()
                        .categoryName(s)
                        .createAt(new Date())
                        .isVisible(true)
                        .productCategories(new HashSet<>())
                        .build();
                categoryRepo.save(category);
            }

            set.clear();

            categoryRepo.findAll().forEach(category -> {
                for (int i = 0; i < 5; i++) {
                    String prodName = new Faker().commerce().productName();
                    if (set.contains(prodName)) {
                        continue;
                    }

                    var product = Product.builder()
                            .productCategory(category)
                            .uuid(UUID.randomUUID().toString())
                            .name(prodName)
                            .defaultKey("default key")
                            .description(new Faker().lorem().characters(50))
                            .price(new BigDecimal(new Faker().commerce().price()))
                            .currency("$")
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
                        detail.addImages(image);
                        detail.setProductSize(size);
                        detail.setProductInventory(inventory);
                        detail.setProductColour(colour);
                        // Add detail to product
                        product.addDetail(detail);
                    }

                    productRepo.save(product);
                    set.add(prodName);
                }
            });
        };
    }

}
