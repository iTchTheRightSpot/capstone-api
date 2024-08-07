package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.HashSet;
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
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = productRepository.findAll();
        assertEquals(2, products.size());

        Product first = products.getFirst();
        Product second = products.get(1);

        // then
        assertEquals(0, productRepository.nameNotAssociatedToUuid(first.getUuid(), "test-1"));
        assertEquals(0, productRepository.nameNotAssociatedToUuid(first.getUuid(), first.getName()));
        assertEquals(1,
                productRepository.nameNotAssociatedToUuid(first.getUuid(), second.getName()));
    }

    @Test
    void allProductsAdminFront() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductProjection> page = productRepository
                .allProductsForAdminFront(SarreCurrency.NGN, PageRequest.of(0, 20));

        assertNotEquals(0, page.getTotalElements());

        for (ProductProjection pojo : page) {
            assertNotNull(pojo.getUuid());
            assertNotNull(pojo.getName());
            assertNotNull(pojo.getDescription());
            assertNotNull(pojo.getPrice());
            assertNotNull(pojo.getCurrency());
            assertNotNull(pojo.getImage());
            assertNotNull(pojo.getWeight());
            assertNotNull(pojo.getWeightType());
            assertNotNull(pojo.getCategory());
        }
    }

    @Test
    void allProductsByCurrencyClient() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductProjection> page = productRepository
                .allProductsByCurrencyClient(SarreCurrency.NGN, PageRequest.of(0, 20));

        assertNotEquals(0, page.getTotalElements());

        for (ProductProjection pojo : page) {
            assertNotNull(pojo.getUuid());
            assertNotNull(pojo.getName());
            assertNotNull(pojo.getDescription());
            assertNotNull(pojo.getPrice());
            assertNotNull(pojo.getCurrency());
            assertNotNull(pojo.getImage());
            assertNotNull(pojo.getWeight());
            assertNotNull(pojo.getWeightType());
            assertNotNull(pojo.getCategory());
        }
    }

    @Test
    void updateProductAndItsCategoryId() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        var collection = categoryRepo
                .save(Category.builder()
                        .name("collection")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());
        var product = products.getFirst();

        // then
        String desc = new Faker().gameOfThrones().dragon();
        productRepository
                .updateProduct(
                        product.getUuid(),
                        "test-1",
                        desc,
                        10.5,
                        collection
                );

        var optional = productRepository.findById(product.getProductId());
        assertFalse(optional.isEmpty());

        Product product1 = optional.get();
        assertEquals("test-1", product1.getName());
        assertEquals(desc, product1.getDescription());
        assertEquals(10.5, product1.getWeight());
        Assertions.assertEquals(collection.getCategoryId(), product1.getProductCategory().getCategoryId());
    }

    @Test
    void productImagesByProductUuid() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());

        List<ImageProjection> images = productRepository
                .productImagesByProductUuid(products.getFirst().getUuid());

        for (ImageProjection pojo : images) {
            assertNotNull(pojo.getImage());
        }
    }

    @Test
    void productsByNameAndCurrency() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductProjection> page = productRepository
                .productsByNameAndCurrency(
                        products.getFirst().getName(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertNotEquals(0, page.getTotalElements());

        for (ProductProjection pojo : page) {
            assertNotNull(pojo.getUuid());
            assertNotNull(pojo.getName());
            assertNull(pojo.getDescription());
            assertNotNull(pojo.getPrice());
            assertNotNull(pojo.getCurrency());
            assertNotNull(pojo.getImage());
            assertNotNull(pojo.getWeight());
            assertNotNull(pojo.getWeightType());
            assertNotNull(pojo.getCategory());
        }
    }

    @Test
    void validateOnDeleteNoActionConstraintWhenDeletingAProductByUuid() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());

        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> productRepository.deleteByProductUuid(products.getFirst().getUuid()));
        assertFalse(currencyRepo.findAll().isEmpty());
    }

    @Test
    void validateOnDeleteCascadeWhenDeletingAProductWithNoDetailsButIsAttachedToPriceCurrency() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        var product = productRepository
                .save(Product.builder()
                        .uuid("uuid")
                        .name("product-1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-image-key")
                        .weight(2.5)
                        .weightType("kg")
                        .categoryId(cat)
                        .productDetails(new HashSet<>())
                        .priceCurrency(new HashSet<>())
                        .build()
                );

        currencyRepo.save(new PriceCurrency(new BigDecimal("45750"), SarreCurrency.NGN, product));
        currencyRepo.save(new PriceCurrency(new BigDecimal("10.52"), SarreCurrency.USD, product));

        // then
        assertFalse(currencyRepo.findAll().isEmpty());
        productRepository.deleteByProductUuid(product.getUuid());
        assertTrue(currencyRepo.findAll().isEmpty());
    }

}