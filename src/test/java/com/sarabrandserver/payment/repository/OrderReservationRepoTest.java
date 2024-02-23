package com.sarabrandserver.payment.repository;

import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.RepositoryTestData;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.*;
import com.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class OrderReservationRepoTest extends AbstractRepositoryTest {

    @Autowired
    private OrderReservationRepo reservationRepo;
    @Autowired
    private ShoppingSessionRepo sessionRepo;
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

    @Test
    void testUpdateQueryForWhenAUserIncreasesTheQtyInTheirOrderReservation() {
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
                .createProduct(2, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());

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

        Date current = new Date();
        ProductSku first = skus.getFirst();
        var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                first.getInventory() - 1,
                                PENDING,
                                CustomUtil.toUTC(
                                        new Date(current
                                                .toInstant()
                                                .plus(15, MINUTES)
                                                .toEpochMilli()
                                        )
                                ),
                                first,
                                session
                        )
                );

        // when
        reservationRepo
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        reservation.getQty(),
                        2,
                        CustomUtil.toUTC(
                                new Date(current
                                        .toInstant()
                                        .plus(20, MINUTES)
                                        .toEpochMilli()
                                )
                        ),
                        "cookie",
                        first.getSku(),
                        PENDING
                );

        // when
        Optional<ProductSku> sku = skuRepo.findById(first.getSkuId());
        assertFalse(sku.isEmpty());
        assertEquals(1, sku.get().getInventory());

        Optional<OrderReservation> res = reservationRepo.findById(reservation.getReservationId());
        assertFalse(res.isEmpty());
        assertEquals(2, res.get().getQty());
    }

    @Test
    void testUpdateQueryForWhenAUserDecreasesTheQtyInTheirOrderReservation() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 2 ProductSku objects
        RepositoryTestData
                .createProduct(2, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());

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

        Date current = new Date();
        ProductSku first = skus.getFirst();
        var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                first.getInventory() - 1,
                                PENDING,
                                CustomUtil.toUTC(
                                        new Date(current
                                                .toInstant()
                                                .plus(15, MINUTES)
                                                .toEpochMilli()
                                        )
                                ),
                                first,
                                session
                        )
                );

        // when
        reservationRepo
                .addToProductSkuInventoryAndReplaceReservationQty(
                        reservation.getQty(),
                        5,
                        CustomUtil.toUTC(
                                new Date(current
                                        .toInstant()
                                        .plus(20, MINUTES)
                                        .toEpochMilli()
                                )
                        ),
                        "cookie",
                        first.getSku(),
                        PENDING
                );

        // when
        Optional<ProductSku> sku = skuRepo.findById(first.getSkuId());
        assertFalse(sku.isEmpty());
        assertTrue(sku.get().getInventory() > first.getInventory());

        Optional<OrderReservation> res = reservationRepo.findById(reservation.getReservationId());
        assertFalse(res.isEmpty());
        assertEquals(5, res.get().getQty());
    }

    @Test
    void allPendingExpiredReservations() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());

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

        Date current = new Date();
        for (ProductSku sku : skus) {
            reservationRepo
                    .save(
                            new OrderReservation(
                                    sku.getInventory() - 1,
                                    PENDING,
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
        }

        // when
        var list = reservationRepo.allPendingExpiredReservations(CustomUtil.toUTC(current), PENDING);
        assertEquals(3, list.size());
    }

    @Test
    void allPendingNoneExpiredReservationsAssociatedToShoppingSession() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());

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

        Date current = new Date();
        for (int i = 0; i < skus.size(); i++) {
            ProductSku curr = skus.get(i);

            Date temp = i % 2 == 0
                    ? new Date(current.toInstant().minus(5, HOURS).toEpochMilli())
                    : new Date(current.toInstant().plus(5, HOURS).toEpochMilli());

            reservationRepo
                    .save(
                            new OrderReservation(
                                    curr.getInventory() - 1,
                                    PENDING,
                                    CustomUtil.toUTC(temp),
                                    curr,
                                    session
                            )
                    );
        }

        // when
        var list = reservationRepo
                .allPendingNoneExpiredReservationsAssociatedToShoppingSession(
                        session.shoppingSessionId(),
                        CustomUtil.toUTC(current),
                        PENDING
                );

        // then
        assertEquals(1, list.size());
    }

    @Test
    void deleteOrderReservationByReservationId() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 1 ProductSku objects
        RepositoryTestData
                .createProduct(1, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(1, skus.size());

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

        Date current = new Date();
        ProductSku first = skus.getFirst();
        var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                first.getInventory() - 1,
                                PENDING,
                                CustomUtil.toUTC(
                                        new Date(current
                                                .toInstant()
                                                .minus(5, HOURS)
                                                .toEpochMilli()
                                        )
                                ),
                                first,
                                session
                        )
                );

        // when
        long reservationId = reservation.getReservationId();
        reservationRepo.deleteOrderReservationByReservationId(reservationId);
        assertTrue(reservationRepo.findById(reservationId).isEmpty());
    }

    @Test
    void deleteExpired() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());

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

        Date current = new Date();
        ProductSku first = skus.getFirst();
        var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                first.getInventory() - 1,
                                PENDING,
                                CustomUtil.toUTC(
                                        new Date(current
                                                .toInstant()
                                                .minus(5, HOURS)
                                                .toEpochMilli()
                                        )
                                ),
                                first,
                                session
                        )
                );

        // when
        reservationRepo.deleteExpiredOrderReservations(CustomUtil.toUTC(new Date()), PENDING);
        assertTrue(reservationRepo.findById(reservation.getReservationId()).isEmpty());
    }

}