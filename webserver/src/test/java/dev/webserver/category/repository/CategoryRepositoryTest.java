package dev.webserver.category.repository;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.entity.ProductCategory;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CategoryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;
    @Autowired
    private ProductImageRepo imageRepo;

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

        RepositoryTestData
                .createProduct(3, category, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);


        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> categoryRepo.deleteProductCategoryById(category.getCategoryId()));
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
            RepositoryTestData
                    .createProduct(3, category, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);
        }

        for (int i = 0; i < 3; i++) {
            RepositoryTestData
                    .createProduct(3, clothes, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);
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
    void shouldUpdateCategoryParentId () {
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

        // method to test
        categoryRepo.updateCategoryParentIdBasedOnCategoryId(
                clothes.getCategoryId(),
                collection.getCategoryId()
        );

        // then
        assertEquals(0, categoryRepo.validateCategoryIsAParent(category.getCategoryId()));
        assertEquals(0, categoryRepo.validateCategoryIsAParent(clothes.getCategoryId()));
        assertEquals(1, categoryRepo.validateCategoryIsAParent(collection.getCategoryId()));
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
            RepositoryTestData
                    .createProduct(3, category, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);
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