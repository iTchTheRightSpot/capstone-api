package dev.webserver.product.repository;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.entity.ProductCategory;
import dev.webserver.category.repository.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.entity.PriceCurrency;
import dev.webserver.product.entity.Product;
import dev.webserver.product.projection.ImagePojo;
import dev.webserver.product.projection.ProductPojo;
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

class ProductRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private PriceCurrencyRepo currencyRepo;

    @Test
    void nameNotAssociatedToUuid() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertEquals(2, products.size());

        Product first = products.getFirst();
        Product second = products.get(1);

        // then
        assertEquals(0, productRepo.nameNotAssociatedToUuid(first.getUuid(), "test-1"));
        assertEquals(0, productRepo.nameNotAssociatedToUuid(first.getUuid(), first.getName()));
        assertEquals(1,
                productRepo.nameNotAssociatedToUuid(first.getUuid(), second.getName()));
    }

    @Test
    void allProductsAdminFront() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductPojo> page = productRepo
                .allProductsForAdminFront(SarreCurrency.NGN, PageRequest.of(0, 20));

        assertNotEquals(0, page.getTotalElements());

        for (ProductPojo pojo : page) {
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
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductPojo> page = productRepo
                .allProductsByCurrencyClient(SarreCurrency.NGN, PageRequest.of(0, 20));

        assertNotEquals(0, page.getTotalElements());

        for (ProductPojo pojo : page) {
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
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        var collection = categoryRepo
                .save(ProductCategory.builder()
                        .name("collection")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());
        var product = products.getFirst();

        // then
        String desc = new Faker().gameOfThrones().dragon();
        productRepo
                .updateProduct(
                        product.getUuid(),
                        "test-1",
                        desc,
                        10.5,
                        collection
                );

        var optional = productRepo.findById(product.getProductId());
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
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        List<ImagePojo> images = productRepo
                .productImagesByProductUuid(products.getFirst().getUuid());

        for (ImagePojo pojo : images) {
            assertNotNull(pojo.getImage());
        }
    }

    @Test
    void productsByNameAndCurrency() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductPojo> page = productRepo
                .productsByNameAndCurrency(
                        products.getFirst().getName(),
                        SarreCurrency.USD,
                        PageRequest.of(0, 20)
                );

        assertNotEquals(0, page.getTotalElements());

        for (ProductPojo pojo : page) {
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
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> productRepo.deleteByProductUuid(products.getFirst().getUuid()));
        assertFalse(currencyRepo.findAll().isEmpty());
    }

    @Test
    void validateOnDeleteCascadeWhenDeletingAProductWithNoDetailsButIsAttachedToPriceCurrency() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        var product = productRepo
                .save(Product.builder()
                        .uuid("uuid")
                        .name("product-1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-image-key")
                        .weight(2.5)
                        .weightType("kg")
                        .productCategory(cat)
                        .productDetails(new HashSet<>())
                        .priceCurrency(new HashSet<>())
                        .build()
                );

        currencyRepo.save(new PriceCurrency(new BigDecimal("45750"), SarreCurrency.NGN, product));
        currencyRepo.save(new PriceCurrency(new BigDecimal("10.52"), SarreCurrency.USD, product));

        // then
        assertFalse(currencyRepo.findAll().isEmpty());
        productRepo.deleteByProductUuid(product.getUuid());
        assertTrue(currencyRepo.findAll().isEmpty());
    }

}