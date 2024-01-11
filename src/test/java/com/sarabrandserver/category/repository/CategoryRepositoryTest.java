package com.sarabrandserver.category.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.ProductRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
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
    @Order(1)
    void allCategories() {
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

        var furniture = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("furniture")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("chair")
                                .isVisible(true)
                                .parentCategory(furniture)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var collection = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("collection")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var fall = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("fall 2024")
                                .isVisible(true)
                                .parentCategory(collection)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("trouser fall 2024")
                                .isVisible(true)
                                .parentCategory(fall)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        // then
        var list = this.categoryRepo.allCategories();

        assertEquals(7, list.size());
        assertEquals(2, list.stream().filter(p -> p.getParent() == null).toList().size());
        assertEquals(5, list.stream().filter(p -> p.getParent() != null).toList().size());
    }

    @Test
    @DisplayName("validate categoryId has 1 or more sub categoryId attached")
    @Order(2)
    void onSubCategory() {
        // given
        var c = categoryRepo
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
                                .parentCategory(c)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        int count = categoryRepo.validateContainsSubCategory(c.getCategoryId());

        assertEquals(2, count);
    }

    @Test
    @DisplayName("validate categoryId has 1 or more products attached")
    @Order(3)
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
    @DisplayName("validate categoryId has 1 or more sub-categoryId and products attached")
    @Order(4)
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
    @DisplayName("""
    Testing query to return all nested child subcategories based on id.
    Visibility for some subcategories are false. Admin front
    """)
    @Order(5)
    void all_categories_admin_front() {
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

        var clothes = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var top = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("top")
                                .isVisible(false)
                                .parentCategory(clothes)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("shirt")
                                .isVisible(true)
                                .parentCategory(top)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        // then
        assertEquals(
                3,
                this.categoryRepo
                        .all_categories_by_categoryId(category.getCategoryId())
                        .size()
        );
    }

}