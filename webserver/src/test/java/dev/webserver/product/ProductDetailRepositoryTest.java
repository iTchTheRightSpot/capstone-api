package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.product.util.Variant;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());

        // then
        assertFalse(detailRepo.productDetailByProductSku(skus.getFirst().sku()).isEmpty());
    }

    @Test
    void updateProductSkuAndProductDetailByProductSku() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var details = TestUtility.toList(detailRepo.findAll());
        assertFalse(details.isEmpty());

        var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        ProductSku sku = skus.getFirst();

        // method to test
        detailRepo
                .updateProductSkuAndProductDetailByProductSku(
                        sku.sku(),
                        "greenish",
                        false,
                        2100,
                        "medium size"
                );

        var optional = skuRepo.productSkuBySku(sku.sku());
        assertFalse(optional.isEmpty());
        ProductSku temp = optional.get();

        assertEquals("medium size", temp.size());
        assertEquals(2100, temp.inventory());
    }

    @Test
    void productDetailByColour() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var details = TestUtility.toList(detailRepo.findAll());
        assertFalse(details.isEmpty());
        ProductDetail first = details.getFirst();

        // then
        var optional = detailRepo.productDetailByColour(first.colour());
        assertFalse(optional.isEmpty());
        assertEquals(first.detailId(), optional.get().detailId());
    }

    @Test
    void shouldReturnProductDetailsByProductUuidForClientFront() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        var product = productRepository.save(
                Product.builder()
                        .uuid("prod-uuid")
                        .name("product 1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-key")
                        .weight(2.5)
                        .weightType("kg")
                        .categoryId(cat.categoryId())
                        .build());

        // check 1
        var detail = detailRepo.save(
                ProductDetail.builder()
                        .colour("green")
                        .isVisible(true)
                        .createAt(LocalDateTime.now())
                        .productId(product.productId())
                        .build());

        for (int i = 0; i < 3; i++) {
            imageRepo.save(new ProductImage(null, UUID.randomUUID().toString(), "path", detail.detailId()));
        }

        for (int i = 0; i < 7; i++) {
            skuRepo.save(
                    ProductSku.builder()
                            .sku("sku " + i)
                            .size("medium " + i)
                            .inventory(i + 3)
                            .detailId(detail.detailId())
                            .build());
        }

        // then
        var details = detailRepo.productDetailsByProductUuidClientFront(product.uuid());
        assertFalse(details.isEmpty());

        for (ProductDetailDbMapper pojo : details) {
            assertNotNull(pojo.isVisible());
            assertNotNull(pojo.colour());
            assertNotNull(pojo.imageKey());

            int size = Arrays.stream(pojo.imageKey().split(",")).toList().size();
            if (pojo.colour().equals("green")) {
                assertEquals(3, size);
            }

            assertNotNull(pojo.variants());
            assertFalse(pojo.variants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.variants(), ProductDetailRepositoryTest.class);
            assertNotNull(array);

            if (pojo.colour().equals("red")) {
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
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        var product = productRepository.save(
                Product.builder()
                        .uuid("prod-uuid")
                        .name("product 1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-key")
                        .weight(2.5)
                        .weightType("kg")
                        .categoryId(cat.categoryId())
                        .build());

        // check 1
        var detail = detailRepo.save(
                ProductDetail.builder()
                        .colour("black")
                        .isVisible(true)
                        .createAt(LocalDateTime.now())
                        .productId(product.productId())
                        .build());

        imageRepo.save(new ProductImage(null, UUID.randomUUID().toString(), "path", detail.detailId()));

        skuRepo.save(
                ProductSku.builder()
                        .sku("sku")
                        .size("medium")
                        .inventory(0)
                        .detailId(detail.detailId())
                        .build());

        // then
        var details = detailRepo
                .productDetailsByProductUuidClientFront(product.uuid());
        assertFalse(details.isEmpty());

        for (ProductDetailDbMapper pojo : details) {
            assertNotNull(pojo.variants());
            assertNotNull(pojo.colour());
            assertNotNull(pojo.imageKey());

            int size = Arrays.stream(pojo.imageKey().split(",")).toList().size();
            if (pojo.colour().equals("black")) {
                assertEquals(1, size);
            }

            assertNotNull(pojo.variants());
            assertFalse(pojo.variants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.variants(), ProductDetailRepositoryTest.class);
            assertNotNull(array);

            if (pojo.colour().equals("red")) {
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
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        var product = productRepository.save(
                Product.builder()
                        .uuid("prod-uuid")
                        .name("product 1")
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey("default-key")
                        .weight(2.5)
                        .weightType("kg")
                        .categoryId(cat.categoryId())
                        .build());

        // check 1
        var detail = detailRepo.save(
                ProductDetail.builder()
                        .colour("red")
                        .isVisible(true)
                        .createAt(LocalDateTime.now())
                        .productId(product.productId())
                        .build());

        for (int i = 0; i < 5; i++)
            imageRepo.save(new ProductImage(null, UUID.randomUUID().toString(), "path", detail.detailId()));

        for (int i = 0; i < 3; i++) {
            skuRepo.save(
                    ProductSku.builder()
                            .sku("sku " + i)
                            .size("medium " + i)
                            .inventory(i + 3)
                            .detailId(detail.detailId())
                            .build());
        }

        // check 2
        var detail2 = detailRepo.save(
                ProductDetail.builder()
                        .colour("brown")
                        .isVisible(true)
                        .createAt(LocalDateTime.now())
                        .productId(product.productId())
                        .build());

        for (int i = 0; i < 2; i++) {
            imageRepo.save(
                    new ProductImage(null, UUID.randomUUID().toString(), "path " + (i + 10), detail2.detailId())
            );
        }

        for (int i = 0; i < 7; i++) {
            skuRepo.save(
                    ProductSku.builder()
                            .sku(UUID.randomUUID().toString())
                            .size("medium " + i)
                            .inventory(i + 3)
                            .detailId(detail2.detailId())
                            .build());
        }

        var res2 = detailRepo
                .productDetailsByProductUuidAdminFront(product.uuid());
        assertEquals(2, res2.size());

        for (ProductDetailDbMapper pojo : res2) {
            assertNotNull(pojo.variants());
            assertNotNull(pojo.colour());
            assertNotNull(pojo.imageKey());

            int size = Arrays.stream(pojo.imageKey().split(",")).toList().size();

            if (pojo.colour().equals("red")) {
                assertEquals(5, size);
            } else {
                assertEquals(2, size);
            }

            assertNotNull(pojo.variants());
            assertFalse(pojo.variants().isEmpty());

            Variant[] array = CustomUtil
                    .toVariantArray(pojo.variants(), ProductDetailRepositoryTest.class);

            assertNotNull(array);

            if (pojo.colour().equals("red")) {
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