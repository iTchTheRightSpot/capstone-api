package com.sarabrandserver.category.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CategoryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private WorkerProductService productService;

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
    void validateOnDeleteNoActionWhenDeletingACategory() {
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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                category.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> categoryRepo.deleteProductCategoryById(category.getCategoryId()));
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
    @DisplayName("""
    validate categoryId has 1 or more sub-categoryId and products
    attached.
    """)
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
    testing query to return all nested child subcategories based on id.
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
                        .allCategoriesByCategoryId(category.getCategoryId())
                        .size()
        );
    }

    @Test
    void allProductsByCategoryIdWhereInStockAndIsVisible() {
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

        var page = categoryRepo
                .allProductsByCategoryIdWhereInStockAndIsVisible(
                        category.getCategoryId(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertEquals(8, page.getNumberOfElements());

        var page1 = categoryRepo
                .allProductsByCategoryIdWhereInStockAndIsVisible(
                        clothes.getCategoryId(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertEquals(3, page1.getNumberOfElements());
    }

    @Test
    void updateAllChildrenVisibilityToFalse() {
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

        // when
        categoryRepo.updateAllChildrenVisibilityToFalse(category.getCategoryId());

        // then
        ProductCategory parent = categoryRepo
                .findById(category.getCategoryId())
                .orElse(null);
        assertNotNull(parent);

        ProductCategory child1 = categoryRepo
                .findById(clothes.getCategoryId())
                .orElse(null);
        assertNotNull(child1);

        ProductCategory child2 = categoryRepo
                .findById(furniture.getCategoryId())
                .orElse(null);
        assertNotNull(child2);

        assertTrue(parent.isVisible());
        assertFalse(child1.isVisible());
        assertFalse(child2.isVisible());
    }

    @Test
    @DisplayName("""
    tests custom query to update a ProductCategory
    parentId
    """)
    void up () {
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
        var collection = categoryRepo
                .save(
                        ProductCategory.builder()
                                .name("collection")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        // when
        categoryRepo
                .updateCategoryParentIdBasedOnCategoryId(
                        clothes.getCategoryId(),
                        collection.getCategoryId()
                );

        // then
        assertEquals(0, categoryRepo.validate_category_is_a_parent(category.getCategoryId()));
        assertEquals(0, categoryRepo.validate_category_is_a_parent(clothes.getCategoryId()));
        assertEquals(1, categoryRepo.validate_category_is_a_parent(collection.getCategoryId()));
    }

    @Test
    void allProductsByCategoryIdAdminFront() {
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

        var page = categoryRepo
                .allProductsByCategoryIdAdminFront(
                        category.getCategoryId(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertEquals(5, page.getNumberOfElements());
    }

}