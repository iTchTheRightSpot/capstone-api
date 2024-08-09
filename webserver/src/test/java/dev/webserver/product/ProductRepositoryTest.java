package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private PriceCurrencyRepository currencyRepo;

    @Test
    void nameNotAssociatedToUuid() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertEquals(2, products.size());

        Product first = products.getFirst();
        Product second = products.get(1);

        // then
        assertEquals(0, productRepository.nameNotAssociatedToUuid(first.uuid(), "test-1"));
        assertEquals(0, productRepository.nameNotAssociatedToUuid(first.uuid(), first.name()));
        assertEquals(1,
                productRepository.nameNotAssociatedToUuid(first.uuid(), second.name()));
    }

    @Test
    void allProductsAdminFront() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        Page<ProductDbMapper> page = productRepository
                .allProductsForAdminFront(SarreCurrency.NGN);

        assertNotEquals(0, page.getTotalElements());

        for (ProductDbMapper pojo : page) {
            assertNotNull(pojo.uuid());
            assertNotNull(pojo.name());
            assertNotNull(pojo.description());
            assertNotNull(pojo.price());
            assertNotNull(pojo.categoryName());
            assertNotNull(pojo.imageKey());
            assertNotNull(pojo.weight());
            assertNotNull(pojo.weightType());
            assertNotNull(pojo.categoryName());
        }
    }

    @Test
    void allProductsByCurrencyClient() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        Page<ProductDbMapper> page = productRepository.allProductsByCurrencyClient(SarreCurrency.NGN);

        assertNotEquals(0, page.getTotalElements());

        for (ProductDbMapper pojo : page) {
            assertNotNull(pojo.uuid());
            assertNotNull(pojo.name());
            assertNotNull(pojo.description());
            assertNotNull(pojo.price());
            assertNotNull(pojo.categoryName());
            assertNotNull(pojo.imageKey());
            assertNotNull(pojo.weight());
            assertNotNull(pojo.weightType());
            assertNotNull(pojo.categoryName());
        }
    }

    @Test
    void updateProductAndItsCategoryId() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        var collection = categoryRepo
                .save(Category.builder()
                        .name("collection")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());
        var product = products.getFirst();

        // then
        String desc = new Faker().gameOfThrones().dragon();
        productRepository
                .updateProduct(
                        product.uuid(),
                        "test-1",
                        desc,
                        10.5,
                        collection.categoryId()
                );

        var optional = productRepository.findById(product.productId());
        assertFalse(optional.isEmpty());

        Product product1 = optional.get();
        assertEquals("test-1", product1.name());
        assertEquals(desc, product1.description());
        assertEquals(10.5, product1.weight());
        Assertions.assertEquals(collection.categoryId(), product1.categoryId());
    }

    @Test
    void productImagesByProductUuid() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        List<ProductImageDbMapper> images = productRepository
                .productImagesByProductUuid(products.getFirst().uuid());

        for (ProductImageDbMapper pojo : images) {
            assertNotNull(pojo.imageKey());
        }
    }

    @Test
    void productsByNameAndCurrency() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        Page<ProductDbMapper> page = productRepository
                .productsByNameAndCurrency(products.getFirst().name(), SarreCurrency.USD);

        assertNotEquals(0, page.getTotalElements());

        for (ProductDbMapper pojo : page) {
            assertNotNull(pojo.uuid());
            assertNotNull(pojo.name());
            assertNotNull(pojo.description());
            assertNotNull(pojo.price());
            assertNotNull(pojo.categoryName());
            assertNotNull(pojo.imageKey());
            assertNotNull(pojo.weight());
            assertNotNull(pojo.weightType());
            assertNotNull(pojo.categoryName());
        }
    }

    @Test
    void validateOnDeleteNoActionConstraintWhenDeletingAProductByUuid() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> productRepository.deleteByProductUuid(products.getFirst().uuid()));
        assertFalse(TestUtility.toList(currencyRepo.findAll()).isEmpty());
    }

    @Test
    void validateOnDeleteCascadeWhenDeletingAProductWithNoDetailsButIsAttachedToPriceCurrency() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        var product = productRepository
                .save(Product.builder()
                        .uuid("uuid")
                        .name("product-1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-image-key")
                        .weight(2.5)
                        .weightType("kg")
                        .categoryId(cat.categoryId())
                        .build());

        currencyRepo.save(new PriceCurrency(null, new BigDecimal("45750"), SarreCurrency.NGN, product.productId()));
        currencyRepo.save(new PriceCurrency(null, new BigDecimal("10.52"), SarreCurrency.USD, product.productId()));

        // then
        assertFalse(TestUtility.toList(currencyRepo.findAll()).isEmpty());
        productRepository.deleteByProductUuid(product.uuid());
        assertTrue(TestUtility.toList(currencyRepo.findAll()).isEmpty());
    }

}