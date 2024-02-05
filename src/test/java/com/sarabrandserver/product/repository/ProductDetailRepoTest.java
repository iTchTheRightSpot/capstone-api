package com.sarabrandserver.product.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.projection.DetailPojo;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ProductDetailRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private WorkerProductService productService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private ProductSkuRepo skuRepo;

    @Test
    void productDetailByProductSku() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

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
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());

        // then
        assertFalse(detailRepo.productDetailByProductSku(skus.getFirst().getSku()).isEmpty());
    }

    @Test
    void updateProductSkuAndProductDetailByProductSku() {
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
        var details = detailRepo.findAll();
        assertFalse(details.isEmpty());

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        ProductSku sku = skus.getFirst();

        // then
        detailRepo
                .updateProductSkuAndProductDetailByProductSku(
                        sku.getSku(),
                        "greenish",
                        false,
                        2100,
                        "medium size"
                );

        var optional = skuRepo.findBySku(sku.getSku());
        assertFalse(optional.isEmpty());
        ProductSku temp = optional.get();

        assertEquals("medium size", temp.getSize());
        assertEquals(2100, temp.getInventory());
    }

    @Test
    void productDetailByColour() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

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
        var details = detailRepo.findAll();
        assertFalse(details.isEmpty());
        ProductDetail first = details.getFirst();

        // then
        var optional = detailRepo.productDetailByColour(first.getColour());
        assertFalse(optional.isEmpty());
        assertEquals(first.getProductDetailId(), optional.get().getProductDetailId());
    }

    @Test
    void productDetailsByProductUuidClient() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

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
        Product product = products.getFirst();

        // then
        var details = detailRepo
                .productDetailsByProductUuidClient(product.getUuid());
        assertFalse(details.isEmpty());

        for (DetailPojo pojo : details) {
            assertNotNull(pojo.getVisible());
            assertNotNull(pojo.getColour());
            assertNotNull(pojo.getImage());
            assertNotNull(pojo.getVariants());
            assertFalse(pojo.getVariants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.getVariants(), ProductDetailRepoTest.class);
            assertNotNull(array);

            for (Variant variant : array) {
                assertFalse(variant.sku().isEmpty());
                String inv = variant.inventory();
                assertFalse(inv.isEmpty());
                assertEquals(0, Integer.parseInt(inv));
                assertFalse(variant.size().isEmpty());
            }
        }
    }

    @Test
    void findProductDetailsByProductUuidWorker() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

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
        Product product = products.getFirst();

        // then
        var details = detailRepo
                .productDetailsByProductUuidWorker(product.getUuid());
        assertFalse(details.isEmpty());

        for (DetailPojo pojo : details) {
            assertNotNull(pojo.getVisible());
            assertNotNull(pojo.getColour());
            assertNotNull(pojo.getImage());
            assertNotNull(pojo.getVariants());
            assertFalse(pojo.getVariants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.getVariants(), ProductDetailRepoTest.class);

            assertNotNull(array);

            for (Variant variant : array) {
                assertFalse(variant.sku().isEmpty());
                String inv = variant.inventory();
                assertFalse(inv.isEmpty());
                assertTrue(Integer.parseInt(inv) > 0);
                assertFalse(variant.size().isEmpty());
            }
        }
    }

}