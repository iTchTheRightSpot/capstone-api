package dev.capstone.product.repository;

import dev.capstone.AbstractRepositoryTest;
import dev.capstone.cart.entity.CartItem;
import dev.capstone.cart.entity.ShoppingSession;
import dev.capstone.cart.repository.CartItemRepo;
import dev.capstone.cart.repository.ShoppingSessionRepo;
import dev.capstone.category.entity.ProductCategory;
import dev.capstone.category.repository.CategoryRepository;
import dev.capstone.data.RepositoryTestData;
import dev.capstone.enumeration.PaymentStatus;
import dev.capstone.enumeration.SarreCurrency;
import dev.capstone.payment.entity.OrderDetail;
import dev.capstone.payment.entity.OrderReservation;
import dev.capstone.payment.entity.PaymentDetail;
import dev.capstone.payment.repository.OrderDetailRepository;
import dev.capstone.payment.repository.OrderReservationRepo;
import dev.capstone.payment.repository.PaymentDetailRepo;
import dev.capstone.product.entity.ProductSku;
import dev.capstone.util.CustomUtil;
import dev.capstone.enumeration.ReservationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ProductSkuRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private OrderDetailRepository orderRepository;
    @Autowired
    private PaymentDetailRepo paymentDetailRepo;
    @Autowired
    private OrderReservationRepo reservationRepo;
    @Autowired
    private ShoppingSessionRepo sessionRepo;
    @Autowired
    private CartItemRepo cartItemRepo;

    @Test
    void updateInventoryOnMakingReservation() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        Assertions.assertNotEquals(0, sku.getInventory());

        skuRepo.updateProductSkuInventoryBySubtractingFromExistingInventory(sku.getSku(), sku.getInventory());

        var optional = skuRepo.productSkuBySku(sku.getSku());
        assertFalse(optional.isEmpty());
        Assertions.assertEquals(0, optional.get().getInventory());
    }

    @Test
    void updateInventory() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        Assertions.assertNotEquals(0, sku.getInventory());

        skuRepo.updateProductSkuInventoryByAddingToExistingInventory(sku.getSku(), sku.getInventory());

        var optional = skuRepo.productSkuBySku(sku.getSku());
        assertFalse(optional.isEmpty());
        assertTrue(optional.get().getInventory() > sku.getInventory());
    }

    @Test
    void validateOnDeleteNoActionConstraintForProductSku() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var paymentDetail = paymentDetailRepo
                .save(
                        PaymentDetail.builder()
                                .name("James Frank")
                                .email("james@email.com")
                                .phone("0000000000")
                                .referenceId("unique-payment-categoryId")
                                .paymentProvider("PayStack")
                                .currency(SarreCurrency.NGN)
                                .amount(new BigDecimal("25750"))
                                .paymentStatus(PaymentStatus.CONFIRMED)
                                .createAt(new Date())
                                .address(null)
                                .orderDetails(new HashSet<>())
                                .build()
                );

        // then
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        ProductSku sku = skus.getFirst();

        // save OrderDetail
        orderRepository.save(new OrderDetail(1, sku, paymentDetail));

        var session = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                CustomUtil.toUTC(new Date(Instant.now().plus(1, HOURS).toEpochMilli())),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        // save OrderReservation
        Date current = new Date();
        reservationRepo
                .save(
                        new OrderReservation(
                                UUID.randomUUID().toString(),
                                sku.getInventory() - 1,
                                ReservationStatus.PENDING,
                                CustomUtil.toUTC(
                                        new Date(current
                                                .toInstant()
                                                .minus(5, HOURS)
                                                .toEpochMilli()
                                        )
                                ),
                                sku,
                                session
                        )
                );

        // save CartItem
        cartItemRepo.save(new CartItem(Integer.MAX_VALUE, session, sku));

        assertThrows(DataIntegrityViolationException.class,
                () -> skuRepo.deleteProductSkuBySku(sku.getSku()));
    }

    @Test
    void validateConstraintProductSkuInvCannotBeLessThanZero() {
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
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());

        assertThrows(JpaSystemException.class,
                () -> skuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                        skus.getFirst().getSku(),
                        -100
                )
        );
    }

    @Test
    void shouldReturnAProductByProductSku() {
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

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());

        // when
        var optional = skuRepo.productByProductSku(skus.getFirst().getSku());

        // then
        assertTrue(optional.isPresent());
    }

}