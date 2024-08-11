package dev.webserver.payment;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.cart.IShoppingSessionRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.RepositoryTestData;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static dev.webserver.enumeration.ReservationStatus.PENDING;
import static org.junit.jupiter.api.Assertions.*;

class OrderReservationRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderReservationRepository reservationRepo;
    @Autowired
    private IShoppingSessionRepository sessionRepo;
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

    @Test
    void testUpdateQueryForWhenAUserIncreasesTheQtyInTheirOrderReservation() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertEquals(2, skus.size());

        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var session = sessionRepo.save(new ShoppingSession(null, "cookie", ldt, ldt.plusHours(1)));

        final ProductSku first = skus.getFirst();
        final var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                null,
                                UUID.randomUUID().toString(),
                                first.inventory() - 1,
                                PENDING,
                                ldt.plusMinutes(15),
                                first.skuId(),
                                session.sessionId()
                        )
                );

        // method to test
        reservationRepo.deductFromProductSkuInventoryAndReplaceReservationQty(
                reservation.qty(),
                2,
                "update",
                ldt.plusMinutes(20),
                "cookie",
                first.sku(),
                PENDING
        );

        // when
        final Optional<ProductSku> sku = skuRepo.findById(first.skuId());
        assertFalse(sku.isEmpty());
        assertEquals(1, sku.get().inventory());

        final Optional<OrderReservation> res = reservationRepo.findById(reservation.reservationId());
        assertFalse(res.isEmpty());
        assertEquals(2, res.get().qty());
    }

    @Test
    void testUpdateQueryForWhenAUserDecreasesTheQtyInTheirOrderReservation() {
        // given
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build()
                );

        // create 2 ProductSku objects
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertEquals(2, skus.size());

        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var session = sessionRepo.save(new ShoppingSession(null, "cookie", ldt, ldt.plusHours(1)));

        final ProductSku first = skus.getFirst();
        final var reservation = reservationRepo
                .save(
                        new OrderReservation(
                                null,
                                UUID.randomUUID().toString(),
                                first.inventory() - 1,
                                PENDING,
                                ldt.plusMinutes(15),
                                first.skuId(),
                                session.sessionId()
                        )
                );

        // when
        reservationRepo
                .addToProductSkuInventoryAndReplaceReservationQty(
                        reservation.qty(),
                        5,
                        "new-reference",
                        ldt.plusMinutes(20),
                        "cookie",
                        first.sku(),
                        PENDING
                );

        // when
        final Optional<ProductSku> sku = skuRepo.findById(first.skuId());
        assertFalse(sku.isEmpty());
        assertTrue(sku.get().inventory() > first.inventory());

        final Optional<OrderReservation> res = reservationRepo.findById(reservation.reservationId());
        assertFalse(res.isEmpty());
        assertEquals(5, res.get().qty());
    }

    @Test
    void allPendingExpiredReservations() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertEquals(3, skus.size());

        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var session = sessionRepo.save(new ShoppingSession(null, "cookie", ldt, ldt.plusHours(1)));

        for (final ProductSku sku : skus) {
            reservationRepo
                    .save(
                            new OrderReservation(
                                    null,
                                    UUID.randomUUID().toString(),
                                    sku.inventory() - 1,
                                    PENDING,
                                    ldt.minusHours(5),
                                    sku.skuId(),
                                    session.sessionId()
                            )
                    );
        }

        // when
        final var list = reservationRepo.allPendingExpiredReservations(ldt, PENDING);
        assertEquals(3, list.size());
    }

    @Test
    void allPendingNoneExpiredReservationsAssociatedToShoppingSession() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertEquals(3, skus.size());

        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var session = sessionRepo.save(new ShoppingSession(null, "cookie", ldt, ldt.plusHours(1)));

        for (int i = 0; i < skus.size(); i++) {
            final ProductSku curr = skus.get(i);

            final LocalDateTime temp = i % 2 == 0 ? ldt.minusHours(5) : ldt.plusHours(5);

            reservationRepo
                    .save(new OrderReservation(
                            null,
                            UUID.randomUUID().toString(),
                            curr.inventory() - 1,
                            PENDING,
                            temp,
                            curr.skuId(),
                            session.sessionId()
                    ));
        }

        // when
        final var list = reservationRepo.allPendingNoneExpiredReservationsAssociatedToShoppingSession(
                session.sessionId(),
                ldt,
                PENDING
        );

        // then
        assertEquals(1, list.size());

        for (final var pojo : list) {
            assertNotNull(pojo.skuId());
            assertNotNull(pojo.reservationId());
            assertNotNull(pojo.qty());
            assertTrue(pojo.qty() > 0);
            assertNotNull(pojo.sku());
            assertFalse(pojo.sku().isEmpty());
        }
    }

    @Test
    void allReservationsByReference() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertEquals(3, skus.size());

        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var session = sessionRepo.save(new ShoppingSession(null, "cookie", ldt, ldt.plusHours(1)));

        final ProductSku first = skus.getFirst();
        reservationRepo
                .save(
                        new OrderReservation(
                                null,
                                UUID.randomUUID().toString(),
                                first.inventory() - 1,
                                PENDING,
                                ldt.minusHours(5),
                                first.skuId(),
                                session.sessionId()
                        )
                );

        // 3 extra reservations
        final String reference = UUID.randomUUID().toString();
        for (int i = 0; i < 3; i++) {
            reservationRepo
                    .save(new OrderReservation(
                            null,
                            reference,
                            first.inventory() - 1,
                            PENDING,
                            ldt.minusHours(5),
                            first.skuId(),
                            session.sessionId())
                    );
        }

        assertEquals(4, TestUtility.toList(reservationRepo.findAll()).size());
        final var list = reservationRepo.allReservationsByReference(reference);
        assertEquals(3, list.size());

        for (final var pojo : list) {
            assertNotNull(pojo.skuId());
            assertNotNull(pojo.reservationId());
            assertNotNull(pojo.qty());
            assertTrue(pojo.qty() > 0);
        }
    }

    @Test
    void shouldSaveOrderReservation() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 3 ProductSku objects
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        final var skus = TestUtility.toList(skuRepo.findAll());
        assertEquals(3, skus.size());

        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var session = sessionRepo.save(new ShoppingSession(null, "cookie", ldt, ldt.plusHours(1)));

        final String reference = UUID.randomUUID().toString();

        reservationRepo.saveOrderReservation(
                reference,
                skus.getFirst().inventory() - 1,
                PENDING,
                ldt,
                skus.getFirst().skuId(),
                session.sessionId()
        );

        reservationRepo.saveOrderReservation(
                reference,
                skus.getFirst().inventory() - 1,
                PENDING,
                ldt,
                skus.getFirst().skuId(),
                session.sessionId()
        );

        assertEquals(2, TestUtility.toList(reservationRepo.findAll()).size());
    }

}