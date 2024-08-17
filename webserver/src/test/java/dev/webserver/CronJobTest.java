package dev.webserver;

import dev.webserver.cart.IShoppingSessionRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.payment.OrderReservation;
import dev.webserver.payment.OrderReservationRepository;
import dev.webserver.payment.PaymentDetailRepository;
import dev.webserver.product.EmployeeProductService;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static dev.webserver.enumeration.ReservationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

final class CronJobTest extends AbstractIntegration {

    @Autowired
    private CronJob cronJob;
    @Autowired
    private EmployeeProductService employeeProductService;
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

    private List<ProductSku> skus() {
        final var category = categoryRepository.save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 4, employeeProductService);

        final var all = TestUtility.toList(skuRepo.findAll());

        Assertions.assertFalse(all.isEmpty());

        return all;
    }

    @Test
    void testOnDeleteOrderReservations_behaviour() {
        if (!Boolean.parseBoolean(System.getProperty("CI_PROFILE"))) {
            return;
        }

        // given
        final var skus = skus();

        final var ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var session = sessionRepo
                .save(new ShoppingSession(null, "cookie", ldt, ldt.minusDays(1)));

        reservationRepo.save(new OrderReservation(
                null,
                "81a39556-3e26-4c1f-a45a-b40342714b4d",
                3,
                PENDING,
                ldt.minusDays(1),
                skus.getFirst().skuId(),
                session.sessionId()
        ));

        reservationRepo.save(new OrderReservation(
                null,
                "ref-dummy",
                3,
                PENDING,
                ldt.minusDays(1),
                skus.get(1).skuId(),
                session.sessionId()
        ));

        reservationRepo.save(new OrderReservation(
                null,
                "ref-dummy-1",
                4,
                PENDING,
                ldt.minusDays(1),
                skus.get(2).skuId(),
                session.sessionId()
        ));

        // method to test
        cronJob.onDeleteOrderReservations();

        // assert
        assertThat(TestUtility.toList(paymentDetailRepository.findAll()).size()).isEqualTo(1);

        assertThat(skuRepo.findById(skus.getFirst().skuId()).orElseThrow().inventory())
                .isEqualTo(skus.getFirst().inventory());

        assertThat(skuRepo.findById(skus.get(1).skuId()).orElseThrow().inventory())
                .isEqualTo(skus.get(1).inventory() + 3);

        assertThat(skuRepo.findById(skus.get(2).skuId()).orElseThrow().inventory())
                .isEqualTo(skus.get(2).inventory() + 4);
    }

}
