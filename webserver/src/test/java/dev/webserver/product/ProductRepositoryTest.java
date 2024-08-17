package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.RepositoryTestData;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.util.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

final class ProductRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private ProductPriceCurrencyRepository productPriceCurrencyRepository;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private ProductPriceCurrencyRepository currencyRepo;

    @Test
    void nameNotAssociatedToUuid() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var products = TestUtility.toList(productRepository.findAll());
        assertEquals(2, products.size());

        final Product first = products.getFirst();
        final Product second = products.get(1);

        // then
        assertEquals(0, productRepository.nameNotAssociatedToUuid(first.uuid(), "test-1"));
        assertEquals(0, productRepository.nameNotAssociatedToUuid(first.uuid(), first.name()));
        assertEquals(1,
                productRepository.nameNotAssociatedToUuid(first.uuid(), second.name()));
    }

    @Test
    void allProductsAdminFront() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var products = TestUtility.toList(productRepository.findAll());
        assertThat(products.isEmpty()).isFalse();

        // method to test
        final var page = productRepository.allProductsForAdminFront(Page.of(0, 20), CapstoneCurrency.NGN);

        // assert
        assertThat(page.size()).isNotEqualTo(0);

        for (final ProductDbMapper pojo : page) {
            assertThat(pojo.uuid()).isNotNull();
            assertThat(pojo.name()).isNotNull();
            assertThat(pojo.description()).isNotNull();
            assertThat(pojo.imageKey()).isNotNull();
            assertThat(pojo.weight()).isNotNull();
            assertThat(pojo.weightType()).isNotNull();
            assertThat(pojo.currency()).isNotNull();
            assertThat(pojo.price()).isNotNull();
            assertThat(pojo.categoryName()).isNotNull();
        }
    }

    @Test
    void updateProductAndItsCategoryId() {
        // given
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        final var collection = categoryRepo
                .save(Category.builder()
                        .name("collection")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());
        final var product = products.getFirst();

        // then
        final String desc = new Faker().gameOfThrones().dragon();
        productRepository
                .updateProduct(
                        product.uuid(),
                        "test-1",
                        desc,
                        new BigDecimal("10.50"),
                        collection.categoryId()
                );

        final var optional = productRepository.findById(product.productId());
        assertFalse(optional.isEmpty());

        final Product product1 = optional.get();
        assertEquals("test-1", product1.name());
        assertEquals(desc, product1.description());
        assertEquals(new BigDecimal("10.50"), product1.weight());
        Assertions.assertEquals(collection.categoryId(), product1.categoryId());
    }

    @Test
    void productImagesByProductUuid() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        final List<ProductImageDbMapper> images = productRepository.productImagesByProductUuid(products.getFirst().uuid());

        assertThat(images.size()).isNotEqualTo(0);

        for (final ProductImageDbMapper pojo : images) assertThat(pojo.imageKey()).isNotNull();
    }

    @Test
    void productsByNameAndCurrency() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        final var list = productRepository.productsByNameAndCurrency(products.getFirst().name(), CapstoneCurrency.USD);

        assertThat(list.isEmpty()).isFalse();

        for (final ProductDbMapper pojo : list) {
            assertThat(pojo.uuid()).isNotNull();
            assertThat(pojo.name()).isNotNull();
            assertThat(pojo.description()).isNotNull();
            assertThat(pojo.currency()).isNotNull();
            assertThat(pojo.price()).isNotNull();
            assertThat(pojo.imageKey()).isNotNull();
            assertThat(pojo.weight()).isNotNull();
            assertThat(pojo.weightType()).isNotNull();
            assertThat(pojo.categoryName()).isNotNull();
        }
    }

    @Test
    void validateOnDeleteNoActionConstraintWhenDeletingAProductByUuid() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData.createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> productRepository.deleteByProductUuid(products.getFirst().uuid()));
        assertFalse(TestUtility.toList(currencyRepo.findAll()).isEmpty());
    }

    @Test
    void validateOnDeleteCascadeWhenDeletingAProductWithNoDetailsButIsAttachedToPriceCurrency() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        final var product = productRepository
                .save(Product.builder()
                        .uuid("uuid")
                        .name("product-1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-image-key")
                        .weight(new BigDecimal("2.5"))
                        .weightType("kg")
                        .categoryId(cat.categoryId())
                        .build());

        currencyRepo.save(new ProductPriceCurrency(null, new BigDecimal("45750"), CapstoneCurrency.NGN, product.productId()));
        currencyRepo.save(new ProductPriceCurrency(null, new BigDecimal("10.52"), CapstoneCurrency.USD, product.productId()));

        // then
        assertFalse(TestUtility.toList(currencyRepo.findAll()).isEmpty());
        productRepository.deleteByProductUuid(product.uuid());
        assertTrue(TestUtility.toList(currencyRepo.findAll()).isEmpty());
    }

    @Test
    void countAllProductsForAdminFront() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // method to test
        assertThat(productRepository.countAllProductsForAdminFront()).isEqualTo(1);
    }

    @Test
    void test_RetrievingProductsToStoreFront() {
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        final var men = categoryRepo.save(Category.builder().name("men").isVisible(true).parentId(cat.categoryId()).build());
        final var tshirt = categoryRepo.save(Category.builder().name("t-shirt").isVisible(false).parentId(men.categoryId()).build());
        final var blue = categoryRepo.save(Category.builder().name("blue t-shirt").isVisible(true).parentId(tshirt.categoryId()).build());

        // 5 products for category
        for (int i = 0; i < 5; i++)
            RepositoryTestData.createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        // 7 products for men
        for (int i = 0; i < 7; i++)
            RepositoryTestData.createProduct(3, men, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        // 2 products for tshirt
        for (int i = 0; i < 2; i++)
            RepositoryTestData.createProduct(3, tshirt, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);
        // 3 products for blue tshirt
        for (int i = 0; i < 3; i++)
            RepositoryTestData.createProduct(3, blue, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // method to test and assert
        assertThat(productRepository.countAllProductsStoreFront()).isEqualTo(12);
        final var list = productRepository.allProductsByCurrencyStoreFront(Page.of(0, 30), CapstoneCurrency.NGN);
        assertThat(list.size()).isEqualTo(12);

        for (final ProductDbMapper pojo : list) {
            assertThat(pojo.uuid()).isNotNull();
            assertThat(pojo.name()).isNotNull();
            assertThat(pojo.description()).isNotNull();
            assertThat(pojo.imageKey()).isNotNull();
            assertThat(pojo.weight()).isNotNull();
            assertThat(pojo.weightType()).isNotNull();
            assertThat(pojo.currency()).isNotNull();
            assertThat(pojo.price()).isNotNull();
            assertThat(pojo.categoryName()).isNotNull();
        }

        // update tshirt visibility
        categoryRepo.update(tshirt.name(), true, tshirt.parentId(), tshirt.categoryId());

        // method to test and assert
        assertThat(productRepository.countAllProductsStoreFront()).isEqualTo(17);
    }

}