package com.sarabrandserver.category.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.projection.ProductPojo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private WorkerProductService productService;

    @AfterEach
    void after() {
        categoryRepo.deleteAll();
        productRepo.deleteAll();
    }

    @Test
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
    @DisplayName("validate categoryId has 1 subcategory attached")
    void on1SubCategory() {
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

        categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("top")
                                .isVisible(true)
                                .parentCategory(clothes)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        assertEquals(1, categoryRepo.validate_category_is_a_parent(category.getCategoryId()));
        assertEquals(1, categoryRepo.validate_category_is_a_parent(clothes.getCategoryId()));
    }

    @Test
    @DisplayName("validate categoryId has 1 or more subcategory attached")
    void onMultipleSubCategory() {
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

        categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("furniture")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        assertEquals(2, categoryRepo.validate_category_is_a_parent(category.getCategoryId()));
    }

    @Test
    @DisplayName("validate categoryId has 1 or more products attached")
    void onProduct() {
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

    @Test
    @DisplayName("""
    test custom query for returning all Products based
    on ProductCategory and its children
    """)
    void productByCategoryId() {
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

        for (int i = 0; i < 5; i++) {
            productService
                    .create(
                            TestData.createProductDTO(
                                    new Faker().commerce().productName() + i,
                                    category.getCategoryId(),
                                    TestData.sizeInventoryDTOArray(3)
                            ),
                            TestData.files()
                    );
        }

        for (int i = 0; i < 3; i++) {
            productService
                    .create(
                            TestData.createProductDTO(
                                    new Faker().commerce().productName() + (i + 30),
                                    clothes.getCategoryId(),
                                    TestData.sizeInventoryDTOArray(3)
                            ),
                            TestData.files()
                    );
        }

        Page<ProductPojo> pojos = categoryRepo
                .productsByCategoryId(
                        category.getCategoryId(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertEquals(8, pojos.getNumberOfElements());
    }

}