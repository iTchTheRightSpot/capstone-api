package dev.webserver.category;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.*;
import dev.webserver.util.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CategoryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private ProductPriceCurrencyRepository productPriceCurrencyRepository;
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
                .createProduct(3, category, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

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
                    .createProduct(3, category, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        for (int i = 0; i < 3; i++) {
            RepositoryTestData
                    .createProduct(3, clothes, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        // method to test
        final var list = categoryRepo.allProductsByCategoryIdWhereInStockAndIsVisible(
                category.categoryId(),
                SarreCurrency.USD,
                Page.of(0, 20)
        );
        final var list1 = categoryRepo.allProductsByCategoryIdWhereInStockAndIsVisible(
                clothes.categoryId(),
                SarreCurrency.USD,
                Page.of(0, 20)
        );

        // assert
        assertEquals(8, list.size());
        list.forEach(p -> {
            assertThat(p.uuid()).isNotNull();
            assertThat(p.name()).isNotNull();
            assertThat(p.imageKey()).isNotNull();
            assertThat(p.description()).isNotNull();
            assertThat(p.price()).isNotNull();
            assertThat(p.currency()).isNotNull();
        });

        assertEquals(3, list1.size());
        list1.forEach(p -> {
            assertThat(p.uuid()).isNotNull();
            assertThat(p.name()).isNotNull();
            assertThat(p.imageKey()).isNotNull();
            assertThat(p.description()).isNotNull();
            assertThat(p.price()).isNotNull();
            assertThat(p.currency()).isNotNull();
        });

        // update children visibility
        categoryRepo.updateCategoryAndAllItsChildrenVisibilityToFalse(clothes.categoryId());

        // method to test
        final var list2 = categoryRepo.allProductsByCategoryIdWhereInStockAndIsVisible(
                category.categoryId(),
                SarreCurrency.USD,
                Page.of(0, 20)
        );

        // assert
        assertEquals(5, list2.size());
        list.forEach(p -> {
            assertThat(p.uuid()).isNotNull();
            assertThat(p.name()).isNotNull();
            assertThat(p.imageKey()).isNotNull();
            assertThat(p.description()).isNotNull();
            assertThat(p.price()).isNotNull();
            assertThat(p.currency()).isNotNull();
        });
    }

    @Test
    void updateCategoryAndAllItsChildrenVisibilityToFalse() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        categoryRepo.save(Category.builder().name("clothes").isVisible(true).parentId(category.categoryId()).build());
        categoryRepo.save(Category.builder().name("furniture").isVisible(true).parentId(category.categoryId()).build());

        // method to test
        categoryRepo.updateCategoryAndAllItsChildrenVisibilityToFalse(category.categoryId());

        // assert
        for (final Category cat : categoryRepo.findAll()) {
            assertThat(cat.isVisible()).isFalse();
        }
    }

    @Test
    void allProductsByCategoryIdAdminFront() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        final var men = categoryRepo.save(Category.builder().name("men").isVisible(true).parentId(category.categoryId()).build());

        for (int i = 0; i < 5; i++) {
            RepositoryTestData
                    .createProduct(3, category, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        for (int i = 0; i < 10; i++) {
            RepositoryTestData
                    .createProduct(3, men, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        // method to test
        final var list = categoryRepo.allProductsByCategoryIdAdminFront(
                category.categoryId(),
                SarreCurrency.USD,
                Page.of(0, 20)
        );

        final var list1 = categoryRepo.allProductsByCategoryIdAdminFront(
                men.categoryId(),
                SarreCurrency.USD,
                Page.of(0, 20)
        );

        // assert
        assertEquals(15, list.size());
        assertEquals(10, list1.size());

        list.forEach(p -> {
            assertThat(p.uuid()).isNotNull();
            assertThat(p.name()).isNotNull();
            assertThat(p.imageKey()).isNotNull();
            assertThat(p.price()).isNotNull();
            assertThat(p.currency()).isNotNull();
        });

        list1.forEach(p -> {
            assertThat(p.uuid()).isNotNull();
            assertThat(p.name()).isNotNull();
            assertThat(p.imageKey()).isNotNull();
            assertThat(p.price()).isNotNull();
            assertThat(p.currency()).isNotNull();
        });
    }

    @Test
    void shouldCountAllProductsByCategoryIdAdminFront() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        final var women = categoryRepo.save(Category.builder().name("women").isVisible(true).parentId(category.categoryId()).build());

        for (int i = 0; i < 5; i++) {
            RepositoryTestData
                    .createProduct(3, category, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        for (int i = 0; i < 20; i++) {
            RepositoryTestData
                    .createProduct(3, women, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        // method to test and assert
        assertThat(categoryRepo.countAllProductsByCategoryIdAdminFront(category.categoryId()))
                .isEqualTo(25);
        assertThat(categoryRepo.countAllProductsByCategoryIdAdminFront(women.categoryId()))
                .isEqualTo(20);
    }

    @Test
    void shouldCountAllProductsByCategoryIdWhereInStockAndIsVisible() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        final var house = categoryRepo.save(Category.builder().name("house").isVisible(true).parentId(category.categoryId()).build());

        for (int i = 0; i < 5; i++) {
            RepositoryTestData
                    .createProduct(3, category, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        for (int i = 0; i < 5; i++) {
            RepositoryTestData
                    .createProduct(3, house, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        }

        // method to test and assert
        assertThat(categoryRepo.countAllProductsByCategoryIdWhereInStockAndIsVisible(category.categoryId())).isEqualTo(10);
        assertThat(categoryRepo.countAllProductsByCategoryIdWhereInStockAndIsVisible(house.categoryId()))
                .isEqualTo(5);

        // update children visibility
        categoryRepo.updateCategoryAndAllItsChildrenVisibilityToFalse(house.categoryId());

        // method to test and assert
        assertThat(categoryRepo.countAllProductsByCategoryIdWhereInStockAndIsVisible(category.categoryId()))
                .isEqualTo(5);
    }

    @Test
    void shouldReturnAllCategoriesForStoreFront() {
        // given
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        final var house = categoryRepo.save(Category.builder().name("house").isVisible(true).parentId(category.categoryId()).build());
        final var den = categoryRepo.save(Category.builder().name("den").isVisible(false).parentId(house.categoryId()).build());
        categoryRepo.save(Category.builder().name("fred").isVisible(true).parentId(den.categoryId()).build());

        // method to test and assert
        assertThat(categoryRepo.allCategoriesStoreFront().size()).isEqualTo(2);

        // update children
        categoryRepo.updateCategoryAndAllItsChildrenVisibilityToFalse(house.categoryId());

        // method to test and assert
        assertThat(categoryRepo.allCategoriesStoreFront().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateACategory() {
        final var category = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        final var clothes = categoryRepo.save(Category.builder().name("clothes").isVisible(true).build());

        // method to test
        categoryRepo.update("t-shirt", false, clothes.categoryId(), category.categoryId());

        // assert
        final var find = categoryRepo.findById(category.categoryId()).orElseThrow();
        assertThat(find.name()).isEqualTo("t-shirt");
        assertThat(find.parentId()).isEqualTo(clothes.categoryId());
        assertThat(find.isVisible()).isFalse();
    }
}