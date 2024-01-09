package com.sarabrandserver.category.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.ProductRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("Test returning all categories query")
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

        categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("collection")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        // then
        List<CategoryPojo> list = this.categoryRepo.allCategories();
        assertEquals(5, list.size());

        List<CategoryResponse> parents = new ArrayList<>(
                list
                        .stream()
                        .filter(p -> p.getParent() == null)
                        .map(p -> new CategoryResponse(p.getId(), null, p.getName(), p.statusImpl(), new ArrayList<>()))
                        .toList()
        );

        list.removeIf(p -> p.getParent() == null);

        assertEquals(2, parents.size());

        list.sort(Comparator.comparing(CategoryPojo::getParent));

        // assert list is sorted based on parent id
        assertTrue(list.size() > 1);

        long parentId = list.getFirst().getParent();

        for (int i = 1; i < list.size(); i++) {
            assertTrue(parentId <= list.get(i).getParent());
            parentId = list.get(i).getParent();
        }

    }

    @Test
    @DisplayName("validate categoryId has 1 or more sub categoryId attached")
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
    Visibility is set to true for all. Store front
    """)
    void a() {
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

        var list = List.of(
                ProductCategory.builder()
                        .name("clothes")
                        .isVisible(true)
                        .parentCategory(c)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build(),
                ProductCategory.builder()
                        .name("furniture")
                        .isVisible(true)
                        .parentCategory(c)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
        );

        categoryRepo.saveAll(list);

        // then
        List<CategoryPojo> res = this.categoryRepo
                .all_categories_store_front(c.getCategoryId());

        assertEquals(2, res.size());
    }

    @Test
    @DisplayName("""
    Testing query to return all nested child subcategories based on id.
    Visibility for some subcategories are false. Store front
    """)
    void all() {
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
                2,
                this.categoryRepo
                        .all_categories_store_front(category.getCategoryId())
                        .size()
        );

        assertEquals(
                1,
                this.categoryRepo
                        .all_categories_store_front(clothes.getCategoryId())
                        .size()
        );
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
                        .all_categories_admin_front(category.getCategoryId())
                        .size()
        );
    }

}