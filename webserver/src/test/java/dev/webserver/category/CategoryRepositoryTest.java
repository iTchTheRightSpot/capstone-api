package dev.webserver.category;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CategoryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepo;

    @Test
    void allCategories() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        categoryRepo.save(Category.builder().name("clothes").isVisible(true).parentId(category.categoryId()).build());

        final var furniture = categoryRepo
                .save(Category.builder().name("furniture").isVisible(true).parentId(category.categoryId()).build());

        categoryRepo.save(Category.builder().name("chair").isVisible(true).parentId(furniture.categoryId()).build());

        final var collection = categoryRepo.save(Category.builder().name("collection").isVisible(true).build());

        final var fall = categoryRepo
                .save(Category.builder().name("fall 2024").isVisible(true).parentId(collection.categoryId()).build());

        categoryRepo.save(Category.builder().name("trouser fall 2024").isVisible(true).parentId(fall.categoryId()).build());

        // then
        final var list = this.categoryRepo.allCategories();

        assertEquals(7, list.size());
        assertEquals(2, list.stream().filter(p -> p.parentId() == null).toList().size());
        assertEquals(5, list.stream().filter(p -> p.parentId() != null).toList().size());
    }

    @Test
    void validateOnDeleteNoActionWhenDeletingACategory() {
        // given
        var category = categoryRepo
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryRepo
                .save(
                        Category.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        RepositoryTestData
                .createProduct(3, category, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);


        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> categoryRepo.deleteProductCategoryById(category.getCategoryId()));
    }

    @Test
    void allProductsByCategoryIdWhereInStockAndIsVisible() {
        // given
        var category = categoryRepo
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var clothes = categoryRepo
                .save(
                        Category.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        for (int i = 0; i < 5; i++) {
            RepositoryTestData
                    .createProduct(3, category, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);
        }

        for (int i = 0; i < 3; i++) {
            RepositoryTestData
                    .createProduct(3, clothes, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);
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
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var clothes = categoryRepo
                .save(
                        Category.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );
        var furniture = categoryRepo
                .save(
                        Category.builder()
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
        Category parent = categoryRepo
                .findById(category.getCategoryId())
                .orElse(null);
        assertNotNull(parent);

        Category child1 = categoryRepo
                .findById(clothes.getCategoryId())
                .orElse(null);
        assertNotNull(child1);

        Category child2 = categoryRepo
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
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var clothes = categoryRepo
                .save(
                        Category.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );
        var collection = categoryRepo
                .save(
                        Category.builder()
                                .name("collection")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        // method to test
        categoryRepo.updateCategoryParentId(
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
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        for (int i = 0; i < 5; i++) {
            RepositoryTestData
                    .createProduct(3, category, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);
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