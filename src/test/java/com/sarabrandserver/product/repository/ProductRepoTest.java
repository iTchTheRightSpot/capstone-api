package com.sarabrandserver.product.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.projection.ImagePojo;
import com.sarabrandserver.product.projection.ProductPojo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ProductRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private WorkerProductService productService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private PriceCurrencyRepo currencyRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private ProductSkuRepo skuRepo;

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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName() + 1,
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductPojo> page = productRepo
                .allProductsAdminFront(NGN, PageRequest.of(0, 20));

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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductPojo> page = productRepo
                .allProductsByCurrencyClient(NGN, PageRequest.of(0, 20));

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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

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
        assertEquals(collection.getCategoryId(), product1.getProductCategory().getCategoryId());
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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        Page<ProductPojo> page = productRepo
                .productsByNameAndCurrency(
                        products.getFirst().getName(),
                        USD,
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

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        assertThrows(DataIntegrityViolationException.class,
                () -> productRepo.deleteByProductUuid(products.getFirst().getUuid()));
        assertFalse(currencyRepo.findAll().isEmpty());
    }

    @Test
    void validateOnDeleteCascadeWhenDeletingAProductWithNoDetailsButIsAttachedToPricesAndImageKeys() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        // when
        skuRepo.deleteAll();
        detailRepo.deleteAll();

        // then
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());
        Product first = products.getFirst();

        assertTrue(productRepo.productImagesByProductUuid(first.getUuid()).isEmpty());
        assertFalse(currencyRepo.findAll().isEmpty());
        assertThrows(DataIntegrityViolationException.class,
                () -> productRepo.deleteByProductUuid(first.getUuid()));
    }

}