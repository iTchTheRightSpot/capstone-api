package dev.webserver.product;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.cart.Cart;
import dev.webserver.cart.ICartRepository;
import dev.webserver.cart.IShoppingSessionRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.RepositoryTestData;
import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.payment.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class ProductSkuRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private ProductPriceCurrencyRepository productPriceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private OrderDetailRepository orderRepository;
    @Autowired
    private PaymentDetailRepository paymentDetailRepository;
    @Autowired
    private OrderReservationRepository reservationRepo;
    @Autowired
    private IShoppingSessionRepository sessionRepo;
    @Autowired
    private ICartRepository iCartRepository;

    @Test
    void updateInventoryOnMakingReservation() {
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build()
                );

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        final var sku = skus.getFirst();

        Assertions.assertNotEquals(0, sku.inventory());

        skuRepo.updateProductSkuInventoryBySubtractingFromExistingInventory(sku.sku(), sku.inventory());

        final var optional = skuRepo.productSkuBySku(sku.sku());
        assertFalse(optional.isEmpty());
        Assertions.assertEquals(0, optional.get().inventory());
    }

    @Test
    void updateInventory() {
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        final var sku = skus.getFirst();

        Assertions.assertNotEquals(0, sku.inventory());

        skuRepo.updateProductSkuInventoryByAddingToExistingInventory(sku.skuId(), sku.inventory());

        final var optional = skuRepo.productSkuBySku(sku.sku());
        assertFalse(optional.isEmpty());
        assertTrue(optional.get().inventory() > sku.inventory());
    }

    @Test
    void validateOnDeleteNoActionConstraintForProductSku() {
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var paymentDetail = paymentDetailRepository
                .save(
                        PaymentDetail.builder()
                                .fullname("James Frank")
                                .email("james@email.com")
                                .phone("0000000000")
                                .referenceId("unique-payment-categoryId")
                                .paymentProvider("PayStack")
                                .currency(CapstoneCurrency.NGN)
                                .amount(new BigDecimal("25750"))
                                .paymentStatus(PaymentStatus.CONFIRMED)
                                .createAt(ldt)
                                .build());

        // then
        final var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        ProductSku sku = skus.getFirst();

        // save OrderDetail
        orderRepository.save(new OrderDetail(null, 1, sku.skuId(), paymentDetail.paymentId()));

        final var session = sessionRepo.save(new ShoppingSession(null, "cookie", ldt, ldt.plusHours(1)));

        // save OrderReservation
        reservationRepo
                .save(
                        new OrderReservation(
                                null,
                                UUID.randomUUID().toString(),
                                sku.inventory() - 1,
                                ReservationStatus.PENDING,
                                ldt.minusHours(5),
                                sku.skuId(),
                                session.sessionId()
                        )
                );

        // save CartItem
        iCartRepository.save(new Cart(null, Integer.MAX_VALUE, session.sessionId(), sku.skuId()));

        assertThrows(DataIntegrityViolationException.class, () -> skuRepo.deleteProductSkuBySku(sku.sku()));
    }

    @Test
    void validateConstraintProductSkuInvCannotBeLessThanZero() {
        // given
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());

        assertThrows(RuntimeException.class,
                () -> skuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                        skus.getFirst().skuId(),
                        -100
                )
        );
    }

    @Test
    void shouldReturnAProductByProductSku() {
        // given
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());

        // when
        final var optional = skuRepo.productByProductSku(skus.getFirst().sku());

        // then
        assertTrue(optional.isPresent());
    }

}