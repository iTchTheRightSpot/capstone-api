package dev.webserver.cron;

import dev.webserver.AbstractIntegration;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.cart.repository.ShoppingSessionRepo;
import dev.webserver.category.entity.ProductCategory;
import dev.webserver.category.repository.CategoryRepository;
import dev.webserver.data.TestData;
import dev.webserver.payment.entity.OrderReservation;
import dev.webserver.payment.repository.OrderReservationRepo;
import dev.webserver.payment.repository.PaymentDetailRepo;
import dev.webserver.product.entity.ProductSku;
import dev.webserver.product.repository.ProductSkuRepo;
import dev.webserver.product.service.WorkerProductService;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;

import static dev.webserver.enumeration.ReservationStatus.PENDING;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CronJobTest extends AbstractIntegration {

    @Autowired
    private CronJob cronJob;
    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private ShoppingSessionRepo sessionRepo;
    @Autowired
    private OrderReservationRepo reservationRepo;
    @Autowired
    private PaymentDetailRepo paymentDetailRepo;

    @Test
    void shouldSuccessfullyValidateFromPayStack() {
        // given
        var sku = productSku();

        var session = sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                CustomUtil.toUTC(Date.from(Instant.now().minus(1, DAYS))),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        reservationRepo
                .save(new OrderReservation(
                        "81a39556-3e26-4c1f-a45a-b40342714b4d",
                        3,
                        PENDING,
                        CustomUtil.toUTC(Date.from(Instant.now().minus(1, DAYS))),
                        sku,
                        session
                ));

        reservationRepo
                .save(new OrderReservation(
                        "ref-dummy",
                        3,
                        PENDING,
                        CustomUtil.toUTC(Date.from(Instant.now().minus(1, DAYS))),
                        sku,
                        session
                ));

        // when
        cronJob.onDeleteOrderReservations();

        // then
        var payments = paymentDetailRepo.findAll();
        var reservations = reservationRepo.findAll();

        assertEquals(1, payments.size());
        assertTrue(reservations.isEmpty());
    }

    private ProductSku productSku() {
        var category = categoryRepository
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .parentCategory(null)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        TestData.dummyProducts(category, 2, workerProductService);

        var all = skuRepo.findAll();

        assertFalse(all.isEmpty());

        return all.getFirst();
    }

}