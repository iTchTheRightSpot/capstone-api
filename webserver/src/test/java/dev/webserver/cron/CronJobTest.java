package dev.webserver.cron;

import dev.webserver.AbstractIntegration;
import dev.webserver.TestUtility;
import dev.webserver.cart.IShoppingSessionRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.TestData;
import dev.webserver.payment.*;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
import dev.webserver.product.WorkerProductService;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static dev.webserver.enumeration.ReservationStatus.PENDING;

class CronJobTest extends AbstractIntegration {

    @Autowired
    private CronJob cronJob;
    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private IShoppingSessionRepository sessionRepo;
    @Autowired
    private OrderReservationRepository reservationRepo;
    @Autowired
    private PaymentDetailRepository paymentDetailRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private PaymentAuthorizationRepository authorizationRepo;

    @Test
    void shouldSuccessfullyValidateFromPayStack() {
        if (!Boolean.parseBoolean(System.getProperty("CI_PROFILE"))) {
            return;
        }

        // given
        var sku = productSku();

        final var ldt = CustomUtil.TO_GREENWICH.apply(null);
        var session = sessionRepo
                .save(
                        new ShoppingSession(
                                null,
                                "cookie",
                                ldt,
                                ldt.minusDays(1)
                        )
                );

        reservationRepo
                .save(new OrderReservation(
                        null,
                        "81a39556-3e26-4c1f-a45a-b40342714b4d",
                        3,
                        PENDING,
                        ldt.minusDays(1),
                        sku.skuId(),
                        session.sessionId()
                ));

        reservationRepo
                .save(new OrderReservation(
                        null,
                        "ref-dummy",
                        3,
                        PENDING,
                        ldt.minusDays(1),
                        sku.skuId(),
                        session.sessionId()
                ));

        // method to test
        cronJob.onDeleteOrderReservations();

        // then
        var payments = TestUtility.toList(paymentDetailRepository.findAll());
        var reservations = TestUtility.toList(reservationRepo.findAll());
        var addresses = TestUtility.toList(addressRepository.findAll());
        var orderDetails = TestUtility.toList(orderDetailRepository.findAll());
        var authorizations = TestUtility.toList(authorizationRepo.findAll());

        Assertions.assertTrue(reservations.isEmpty());
        Assertions.assertEquals(1, payments.size());
        Assertions.assertEquals(1, addresses.size());
        Assertions.assertEquals(1, authorizations.size());
        Assertions.assertFalse(orderDetails.isEmpty());
    }

    private ProductSku productSku() {
        var category = categoryRepository.save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 2, workerProductService);

        var all = TestUtility.toList(skuRepo.findAll());

        Assertions.assertFalse(all.isEmpty());

        return all.getFirst();
    }

}
