package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.entity.ProductImage;
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
    public CommandLineRunner runner(
            CategoryRepository categoryRepo,
            CollectionRepository collRepo,
            ProductRepository productRepo,
            ProductDetailRepo detailRepo
    ) {
        return args -> {
            extracted(categoryRepo, collRepo, productRepo, detailRepo);
        };
    }

    private static void extracted(CategoryRepository categoryRepo, CollectionRepository collRepo, ProductRepository productRepo, ProductDetailRepo detailRepo) {
        categoryRepo.deleteAll();
        collRepo.deleteAll();
        detailRepo.deleteAll();
        productRepo.deleteAll();


        Set<String> set = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            set.add(new Faker().commerce().department());
        }

        ProductCategory ca = null;
        int i = 0;
        for (String s : set) {
            var category = ProductCategory.builder()
                    .categoryName(s)
                    .createAt(new Date())
                    .isVisible(true)
                    .productCategories(new HashSet<>())
                    .build();
            var saved = categoryRepo.save(category);

            if (i == 0) {
                ca = saved;
            }
            i++;
        }

        var product = Product.builder()
                .productCategory(ca)
                .uuid(UUID.randomUUID().toString())
                .name("Product1")
                .description(new Faker().lorem().characters(200))
                .defaultKey("Unique ID")
                .price(new BigDecimal(50))
                .currency("USD") // default is USD
                .productDetails(new HashSet<>())
                .build();

        var product1 = Product.builder()
                .productCategory(ca)
                .uuid(UUID.randomUUID().toString())
                .name("Product2")
                .description(new Faker().lorem().characters(200))
                .defaultKey("Unique ID 2")
                .price(new BigDecimal(5))
                .currency("USD") // default is USD
                .productDetails(new HashSet<>())
                .build();

        var savedProduct = productRepo.save(product);
        var savedProduct1 = productRepo.save(product1);

        var detail = ProductDetail.builder()
                .product(savedProduct)
                .colour("Green")
                .createAt(new Date())
                .isVisible(true)
                .productImages(new HashSet<>())
                .build();

        var detail1 = ProductDetail.builder()
                .product(savedProduct1)
                .colour("Blue")
                .createAt(new Date())
                .isVisible(true)
                .productImages(new HashSet<>())
                .build();

        for (int j = 0; j < 3; j++) {
            var image = ProductImage.builder()
                    .imageKey(UUID.randomUUID().toString())
                    .imagePath("/paths")
                    .build();
            detail.addImages(image);
        }

        for (int j = 0; j < 3; j++) {
            var image = ProductImage.builder()
                    .imageKey(UUID.randomUUID().toString())
                    .imagePath("/paths")
                    .build();
            detail1.addImages(image);
        }

        detailRepo.save(detail);
        detailRepo.save(detail1);
    }

}
