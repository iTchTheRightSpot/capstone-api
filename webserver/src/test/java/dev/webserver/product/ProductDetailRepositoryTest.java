package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.ProductCategory;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.product.util.Variant;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductDetailRepositoryTest extends AbstractRepositoryTest {

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

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

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
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

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

        var optional = skuRepo.productSkuBySku(sku.getSku());
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

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

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
    void shouldReturnProductDetailsByProductUuidForClientFront() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        var product = productRepository.save(
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
                        .colour("green")
                        .isVisible(true)
                        .createAt(new Date())
                        .product(product)
                        .productImages(new HashSet<>())
                        .skus(new HashSet<>())
                        .build()
        );

        for (int i = 0; i < 3; i++) {
            imageRepo.save(new ProductImage(UUID.randomUUID().toString(), "path", detail));
        }

        for (int i = 0; i < 7; i++) {
            skuRepo.save(
                    ProductSku.builder()
                            .sku("sku " + i)
                            .size("medium " + i)
                            .inventory(i + 3)
                            .productDetail(detail)
                            .orderDetails(new HashSet<>())
                            .reservations(new HashSet<>())
                            .cartItems(new HashSet<>())
                            .build()
            );
        }

        // then
        var details = detailRepo
                .productDetailsByProductUuidClientFront(product.getUuid());
        assertFalse(details.isEmpty());

        for (DetailProjection pojo : details) {
            assertNotNull(pojo.getVisible());
            assertNotNull(pojo.getColour());
            assertNotNull(pojo.getImage());

            int size = Arrays.stream(pojo.getImage().split(",")).toList().size();
            if (pojo.getColour().equals("green")) {
                assertEquals(3, size);
            }

            assertNotNull(pojo.getVariants());
            assertFalse(pojo.getVariants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.getVariants(), ProductDetailRepositoryTest.class);
            assertNotNull(array);

            if (pojo.getColour().equals("red")) {
                assertEquals(7, array.length);
            }

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
    void shouldReturnProductDetailsByProductUuidWhereInvIsZeroForClientFront() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        var product = productRepository.save(
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
                        .colour("black")
                        .isVisible(true)
                        .createAt(new Date())
                        .product(product)
                        .productImages(new HashSet<>())
                        .skus(new HashSet<>())
                        .build()
        );

        imageRepo.save(new ProductImage(UUID.randomUUID().toString(), "path", detail));

        skuRepo.save(
                ProductSku.builder()
                        .sku("sku")
                        .size("medium")
                        .inventory(0)
                        .productDetail(detail)
                        .orderDetails(new HashSet<>())
                        .reservations(new HashSet<>())
                        .cartItems(new HashSet<>())
                        .build()
        );

        // then
        var details = detailRepo
                .productDetailsByProductUuidClientFront(product.getUuid());
        assertFalse(details.isEmpty());

        for (DetailProjection pojo : details) {
            assertNotNull(pojo.getVisible());
            assertNotNull(pojo.getColour());
            assertNotNull(pojo.getImage());

            int size = Arrays.stream(pojo.getImage().split(",")).toList().size();
            if (pojo.getColour().equals("black")) {
                assertEquals(1, size);
            }

            assertNotNull(pojo.getVariants());
            assertFalse(pojo.getVariants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.getVariants(), ProductDetailRepositoryTest.class);
            assertNotNull(array);

            if (pojo.getColour().equals("red")) {
                assertEquals(1, array.length);
            }

            for (Variant variant : array) {
                assertFalse(variant.sku().isEmpty());
                String inv = variant.inventory();
                assertFalse(inv.isEmpty());
                assertEquals(-1, Integer.parseInt(inv));
                assertFalse(variant.size().isEmpty());
            }
        }
    }

    @Test
    void shouldReturnProductDetailsByProductUuidForAdminFront() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        var product = productRepository.save(
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
                            .size("medium " + i)
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
                        .colour("brown")
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
                            .size("medium " + i)
                            .inventory(i + 3)
                            .productDetail(detail2)
                            .orderDetails(new HashSet<>())
                            .reservations(new HashSet<>())
                            .cartItems(new HashSet<>())
                            .build()
            );
        }

        var res2 = detailRepo
                .productDetailsByProductUuidAdminFront(product.getUuid());
        assertEquals(2, res2.size());

        for (DetailProjection pojo : res2) {
            assertNotNull(pojo.getVisible());
            assertNotNull(pojo.getColour());
            assertNotNull(pojo.getImage());
            int size = Arrays.stream(pojo.getImage().split(",")).toList().size();
            if (pojo.getColour().equals("red")) {
                assertEquals(5, size);
            } else {
                assertEquals(2, size);
            }

            assertNotNull(pojo.getVariants());
            assertFalse(pojo.getVariants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.getVariants(), ProductDetailRepositoryTest.class);

            assertNotNull(array);

            if (pojo.getColour().equals("red")) {
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