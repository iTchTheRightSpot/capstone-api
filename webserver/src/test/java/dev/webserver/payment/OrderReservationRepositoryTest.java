package dev.webserver.payment;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.cart.ShoppingSessionRepository;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static dev.webserver.enumeration.ReservationStatus.PENDING;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;

class OrderReservationRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderReservationRepository reservationRepo;
    @Autowired
    private ShoppingSessionRepository sessionRepo;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private ProductSkuRepository skuRepo;

    @Test
    void testUpdateQueryForWhenAUserIncreasesTheQtyInTheirOrderReservation() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());

        var session = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                CustomUtil.toUTC(Date.from(Instant.now().plus(1, HOURS))),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        Date current = new Date();
        ProductSku first = skus.getFirst();
        var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                UUID.randomUUID().toString(),
                                first.getInventory() - 1,
                                PENDING,
                                CustomUtil.toUTC(
                                        Date.from(current
                                                .toInstant()
                                                .plus(15, MINUTES)
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
                        "update",
                        CustomUtil.toUTC(
                                Date.from(current
                                        .toInstant()
                                        .plus(20, MINUTES)
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
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 2 ProductSku objects
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());

        var session = this.sessionRepo
                .save(new ShoppingSession(
                        "cookie",
                        new Date(),
                        CustomUtil.toUTC(Date.from(Instant.now().plus(1, HOURS))),
                        new HashSet<>(),
                        new HashSet<>()
                ));

        Date current = new Date();
        ProductSku first = skus.getFirst();
        var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                UUID.randomUUID().toString(),
                                first.getInventory() - 1,
                                PENDING,
                                CustomUtil.toUTC(
                                        Date.from(current
                                                .toInstant()
                                                .plus(15, MINUTES)
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
                        "new-reference",
                        CustomUtil.toUTC(
                                Date.from(current
                                        .toInstant()
                                        .plus(20, MINUTES)
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
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());

        var session = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                CustomUtil.toUTC(Date.from(Instant.now().plus(1, HOURS))),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        Date current = new Date();
        for (ProductSku sku : skus) {
            reservationRepo
                    .save(
                            new OrderReservation(
                                    UUID.randomUUID().toString(),
                                    sku.getInventory() - 1,
                                    PENDING,
                                    CustomUtil.toUTC(
                                            Date.from(current
                                                    .toInstant()
                                                    .minus(5, HOURS)
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
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());

        var session = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                CustomUtil.toUTC(Date.from(Instant.now().plus(1, HOURS))),
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
                    .save(new OrderReservation(
                            UUID.randomUUID().toString(),
                            curr.getInventory() - 1,
                            PENDING,
                            CustomUtil.toUTC(temp),
                            curr,
                            session
                    ));
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

        for (var pojo : list) {
            assertTrue(pojo.getReservationId() > 0);
            assertTrue(pojo.getReservationQty() > 0);
            assertFalse(pojo.getProductSkuSku().isEmpty());
        }
    }

    @Test
    void allReservationsByReference() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

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
        reservationRepo
                .save(
                        new OrderReservation(
                                UUID.randomUUID().toString(),
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

        // 3 extra reservations
        String reference = UUID.randomUUID().toString();
        for (int i = 0; i < 3; i++) {
            reservationRepo
                    .save(new OrderReservation(
                            reference,
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
                            session)
                    );
        }

        assertEquals(4, reservationRepo.findAll().size());
        var list = reservationRepo.allReservationsByReference(reference);
        assertEquals(3, list.size());

        for (var pojo : list) {
            assertTrue(pojo.getReservationId() > 0);
            assertTrue(pojo.getReservationQty() > 0);
            assertTrue(pojo.getProductSkuId() > 0);
        }
    }

    @Test
    void shouldSaveOrderReservation() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());

        var session = this.sessionRepo
                .save(new ShoppingSession(
                        "cookie",
                        new Date(),
                        CustomUtil.toUTC(new Date(Instant.now().plus(1, HOURS).toEpochMilli())),
                        new HashSet<>(),
                        new HashSet<>()
                ));

        String reference = UUID.randomUUID().toString();

        reservationRepo.saveOrderReservation(
                reference,
                skus.getFirst().getInventory() - 1,
                PENDING,
                new Date(),
                skus.getFirst().getSkuId(),
                session.shoppingSessionId()
        );

        reservationRepo.saveOrderReservation(
                reference,
                skus.getFirst().getInventory() - 1,
                PENDING,
                new Date(),
                skus.getFirst().getSkuId(),
                session.shoppingSessionId()
        );

        assertEquals(2, reservationRepo.findAll().size());
    }

}