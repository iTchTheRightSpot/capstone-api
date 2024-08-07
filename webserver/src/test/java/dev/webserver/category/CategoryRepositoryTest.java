package dev.webserver.category;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

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
        final var list = categoryRepo.allCategories();

        assertEquals(7, list.size());
        assertEquals(2, list.stream().filter(p -> p.parentId() == null).toList().size());
        assertEquals(5, list.stream().filter(p -> p.parentId() != null).toList().size());
    }

    @Test
    void validateOnDeleteNoActionWhenDeletingACategory() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        categoryRepo.save(Category.builder().name("clothes").isVisible(true).parentId(category.categoryId()).build());

        RepositoryTestData
                .createProduct(3, category, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // method to test and assert
        assertThrows(DataIntegrityViolationException.class, () -> categoryRepo.deleteProductCategoryById(category.categoryId()));
    }

    @Test
    void allProductsByCategoryIdWhereInStockAndIsVisible() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        final var clothes = categoryRepo
                .save(Category.builder().name("clothes").isVisible(true).parentId(category.categoryId()).build());

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
                        category.categoryId(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertEquals(8, page.getNumberOfElements());

        var page1 = categoryRepo
                .allProductsByCategoryIdWhereInStockAndIsVisible(
                        clothes.categoryId(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertEquals(3, page1.getNumberOfElements());
    }

    @Test
    void updateAllChildrenVisibilityToFalse() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        final var clothes = categoryRepo
                .save(Category.builder().name("clothes").isVisible(true).parentId(category.categoryId()).build());

        final var furniture = categoryRepo
                .save(Category.builder().name("furniture").isVisible(true).parentId(category.categoryId()).build());

        // method to test
        categoryRepo.updateAllChildrenVisibilityToFalse(category.categoryId());

        // then
        final Category parent = categoryRepo.findById(category.categoryId()).orElse(null);
        assertNotNull(parent);

        final Category child1 = categoryRepo.findById(clothes.categoryId()).orElse(null);
        assertNotNull(child1);

        final Category child2 = categoryRepo.findById(furniture.categoryId()).orElse(null);
        assertNotNull(child2);

        assertTrue(parent.isVisible());
        assertFalse(child1.isVisible());
        assertFalse(child2.isVisible());
    }

    @Test
    void shouldUpdateCategoryParentId () {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        final var clothes = categoryRepo
                .save(Category.builder().name("clothes").isVisible(true).parentId(category.categoryId()).build());

        final var collection = categoryRepo.save(Category.builder().name("collection").isVisible(true).build());

        // method to test
        categoryRepo.updateCategoryParentId(clothes.categoryId(), collection.categoryId());

        // then
        assertEquals(0, categoryRepo.validateCategoryIsAParent(category.categoryId()));
        assertEquals(0, categoryRepo.validateCategoryIsAParent(clothes.categoryId()));
        assertEquals(1, categoryRepo.validateCategoryIsAParent(collection.categoryId()));
    }

    @Test
    void allProductsByCategoryIdAdminFront() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        for (int i = 0; i < 5; i++) {
            RepositoryTestData
                    .createProduct(3, category, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);
        }

        var page = categoryRepo
                .allProductsByCategoryIdAdminFront(
                        category.categoryId(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertEquals(5, page.getNumberOfElements());
    }

}