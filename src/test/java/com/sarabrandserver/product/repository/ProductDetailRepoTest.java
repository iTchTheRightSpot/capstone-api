package com.sarabrandserver.product.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.entity.ProductImage;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.projection.DetailPojo;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

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
    @Autowired
    private ProductImageRepo imageRepo;

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
    void productDetailsByProductUuidWorker() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        var product = productRepo.save(
                Product.builder()
                        .uuid("prod-uuid")
                        .name("product 1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-key")
                        .weight(2.5)
                        .weightType("kg")
                        .productCategory(cat)
                        .productDetails(new HashSet<>())
                        .priceCurrency(new HashSet<>())
                        .build()
        );

        // check 1
        var detail = detailRepo.save(
                ProductDetail.builder()
                        .colour("red")
                        .isVisible(true)
                        .createAt(new Date())
                        .product(product)
                        .productImages(new HashSet<>())
                        .skus(new HashSet<>())
                        .build()
        );

        for (int i = 0; i < 5; i++) {
            imageRepo.save(
                    new ProductImage(UUID.randomUUID().toString(), "path", detail)
            );
        }

        for (int i = 0; i < 3; i++) {
            skuRepo.save(
                    ProductSku.builder()
                            .sku("sku " + i)
                            .size(new Faker().bothify(""))
                            .inventory(i + 3)
                            .productDetail(detail)
                            .orderDetails(new HashSet<>())
                            .reservations(new HashSet<>())
                            .cartItems(new HashSet<>())
                            .build()
            );
        }

        // check 2
        var detail2 = detailRepo.save(
                ProductDetail.builder()
                        .colour("red")
                        .isVisible(true)
                        .createAt(new Date())
                        .product(product)
                        .productImages(new HashSet<>())
                        .skus(new HashSet<>())
                        .build()
        );

        for (int i = 0; i < 2; i++) {
            imageRepo.save(
                    new ProductImage(UUID.randomUUID().toString(), "path " + (i + 10), detail2)
            );
        }

        for (int i = 0; i < 7; i++) {
            skuRepo.save(
                    ProductSku.builder()
                            .sku(UUID.randomUUID().toString())
                            .size(new Faker().bothify(""))
                            .inventory(i + 3)
                            .productDetail(detail2)
                            .orderDetails(new HashSet<>())
                            .reservations(new HashSet<>())
                            .cartItems(new HashSet<>())
                            .build()
            );
        }

        var res2 = detailRepo
                .productDetailsByProductUuidWorker(product.getUuid());
        assertFalse(res2.isEmpty());
        assertEquals(2, res2.size());

        for (int i = 0; i < res2.size(); i++) {
            DetailPojo pojo = res2.get(i);

            assertNotNull(pojo.getVisible());
            assertNotNull(pojo.getColour());
            assertNotNull(pojo.getImage());
            int size = Arrays.stream(pojo.getImage().split(",")).toList().size();
            if (i == 0) {
                assertEquals(5, size);
            } else {
                assertEquals(2, size);
            }

            assertNotNull(pojo.getVariants());
            assertFalse(pojo.getVariants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.getVariants(), ProductDetailRepoTest.class);

            assertNotNull(array);

            if (i == 0) {
                assertEquals(3, array.length);
            } else {
                assertEquals(7, array.length);
            }

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