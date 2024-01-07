package com.sarabrandserver.category.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.ProductRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;

    @AfterEach
    void after() {
        categoryRepo.deleteAll();
        productRepo.deleteAll();
    }

    @Test
    @DisplayName("validate category has 1 or more sub category attached")
    void onSubCategory() {
        // given
        var category = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        int parentCount = categoryRepo.validateContainsSubCategory(category.getCategoryId());

        assertEquals(1, parentCount);
    }

    @Test
    @DisplayName("validate category has 1 or more products attached")
    void OnProduct() {
        var category = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        productRepo
                .save(
                        Product.builder()
                                .uuid(UUID.randomUUID().toString())
                                .name(new Faker().commerce().productName())
                                .description(new Faker().lorem().fixedString(40))
                                .defaultKey("default-key")
                                .productCategory(category)
                                .productDetails(new HashSet<>())
                                .priceCurrency(new HashSet<>())
                                .build()
                );

        int count = categoryRepo.validateProductAttached(category.getCategoryId());

        assertEquals(1, count);
    }

    @Test
    @DisplayName("validate category has 1 or more sub-category and products attached")
    void validateOnDelete() {
        var category = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var subCategory = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        productRepo
                .save(
                        Product.builder()
                                .uuid(UUID.randomUUID().toString())
                                .name(new Faker().commerce().productName())
                                .description(new Faker().lorem().fixedString(40))
                                .defaultKey("default-key")
                                .productCategory(subCategory)
                                .productDetails(new HashSet<>())
                                .priceCurrency(new HashSet<>())
                                .build()
                );

        int count = categoryRepo.validateProductAttached(category.getCategoryId());

        assertEquals(1, count);
    }

    @Test
    void all_categories_store_front() {
    }

    @Test
    void all_categories_admin_front() {
    }

}