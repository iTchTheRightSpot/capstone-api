package com.emmanuel.sarabrandserver.test;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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

    private static void extracted(
            CategoryRepository categoryRepo,
            CollectionRepository collRepo,
            ProductRepository productRepo,
            ProductDetailRepo detailRepo
    ) {
        categoryRepo.deleteAll();
        collRepo.deleteAll();
        detailRepo.deleteAll();
        productRepo.deleteAll();

        Set<String> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String s : set) {
            var category = ProductCategory.builder()
                    .uuid(UUID.randomUUID().toString())
                    .categoryName(s)
                    .createAt(new Date())
                    .isVisible(true)
                    .productCategories(new HashSet<>())
                    .build();
            categoryRepo.save(category);
        }

        set.clear();

        for (int i = 0; i < 5; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String s : set) {
            var collection = ProductCollection.builder()
                    .collection(s)
                    .isVisible(true)
                    .createAt(new Date())
                    .modifiedAt(null)
                    .products(new HashSet<>())
                    .build();
            collRepo.save(collection);
        }

    }

}
