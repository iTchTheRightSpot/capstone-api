package dev.webserver.cron;

import com.fasterxml.jackson.databind.JsonNode;
import dev.webserver.cart.entity.CartItem;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.cart.repository.CartItemRepo;
import dev.webserver.cart.repository.ShoppingSessionRepo;
import dev.webserver.payment.entity.OrderReservation;
import dev.webserver.payment.repository.OrderReservationRepo;
import dev.webserver.payment.service.PaymentDetailService;
import dev.webserver.product.entity.ProductSku;
import dev.webserver.product.repository.ProductSkuRepo;
import dev.webserver.thirdparty.ThirdPartyPaymentService;
import dev.webserver.util.CustomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static dev.webserver.enumeration.ReservationStatus.PENDING;
import static org.springframework.http.HttpStatus.*;

@Component
@Transactional(rollbackFor = Exception.class)
class CronJob {

    private static final Logger log = LoggerFactory.getLogger(CronJob.class);

    private final RestClient restClient;
    private final ProductSkuRepo skuRepo;
    private final OrderReservationRepo reservationRepo;
    private final ShoppingSessionRepo sessionRepo;
    private final CartItemRepo cartItemRepo;
    private final PaymentDetailService paymentDetailService;
    private final String secretKey;

    public CronJob(
            RestClient.Builder clientBuilder,
            ProductSkuRepo skuRepo,
            OrderReservationRepo reservationRepo,
            ShoppingSessionRepo sessionRepo,
            CartItemRepo cartItemRepo,
            PaymentDetailService paymentDetailService,
            ThirdPartyPaymentService paymentService
    ) {
        this.skuRepo = skuRepo;
        this.reservationRepo = reservationRepo;
        this.sessionRepo = sessionRepo;
        this.cartItemRepo = cartItemRepo;
        this.paymentDetailService = paymentDetailService;
        this.secretKey = paymentService.payStackCredentials().secretKey();
        this.restClient = clientBuilder.build();
    }

    /**
     * Cron job to run every 15 mins.
     * @see <a href="https://docs.spring.io/spring-framework/reference/integration/scheduling.html">documentation</a>
     * */
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES, zone = "UTC")
    public void schedule() {
        onDeleteShoppingSessions();
        onDeleteOrderReservations();
    }

    /**
     * Deletes all expired {@link ShoppingSession} instances and their associated {@link CartItem}.
     * This method retrieves all expired {@link ShoppingSession} instances from the repository
     * using the current date as a reference point. For each expired session, it deletes all
     * {@link CartItem} associated with that session and then deletes the session itself.
     */
    @Transactional(rollbackFor = Exception.class)
    public void onDeleteShoppingSessions() {
        sessionRepo.allExpiredShoppingSession(CustomUtil.toUTC(new Date()))
                .forEach(session -> {
                    this.cartItemRepo.deleteCartItemsByShoppingSessionId(
                            session.shoppingSessionId());
                    this.sessionRepo.deleteById(session.shoppingSessionId());
                });
    }

    /**
     * Retrieves all expired {@link OrderReservation}s with a pending status for deletion.
     * After the {@link OrderReservation}s have been retrieved, update the inventory of
     * associated {@link ProductSku}s since the user did
     * not complete the purchase within the allotted time. After the update has been performed,
     * deletion of {@link OrderReservation} is performed.
     * Note: The timeout for every payment session in Paystack is 600 seconds or 10 minutes.
     * @see
     * <a href="https://paystack.com/docs/api/integration/#update-timeout">updating the timeout</a>.
     */
    public void onDeleteOrderReservations() {
        var date = CustomUtil
                .toUTC(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)));

        var reservations = reservationRepo.allPendingExpiredReservations(date, PENDING);

        onResponseFromPaystack(reservations)
                .stream()
                .filter(reservation -> onSuccess(reservation)
                        || reservation.status().equals(BAD_REQUEST)
                        || reservation.status().equals(NOT_FOUND)
                )
                .forEach(obj -> {
                    if (obj.status().equals(OK)) {
                        var email = obj.node().get("data").get("customer").get("email").textValue();
                        var reference = obj.node().get("data").get("reference").textValue();

                        if (!paymentDetailService.paymentDetailExists(email, reference)) {
                            paymentDetailService.onSuccessfulPayment(obj.node().get("data"));
                        }
                    }

                    skuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                            obj.reservation().getProductSku().getSku(),
                            obj.reservation().getQty()
                    );

                    reservationRepo.deleteOrderReservationByReservationId(
                            obj.reservation().getReservationId()
                    );
                });
    }

    /**
     * Leveraging multithreading, function validates the status of expired {@link OrderReservation}s
     * from Paystack.
     *
     * @param reservations The {@link List} of expired {@link OrderReservation} objects to validate.
     * @return A {@link List} of {@link CustomCronJobObject} which contains validated
     * {@link OrderReservation}s.
     * @see <a href="https://paystack.com/docs/payments/verify-payments/">documentation</a>
     */
    private List<CustomCronJobObject> onResponseFromPaystack(
            final List<OrderReservation> reservations
    ) {
        var futures = reservations.stream()
                .map(reservation -> (Supplier<CustomCronJobObject>) () -> {
                    var uri = UriComponentsBuilder
                            .fromUriString("https://api.paystack.co/transaction/verify")
                            .pathSegment(reservation.getReference())
                            .build()
                            .toUri();

                    try {
                        var node = restClient
                                .get()
                                .uri(uri)
                                .header("Authorization", "Bearer %s".formatted(secretKey))
                                .retrieve()
                                .body(JsonNode.class);

                        return new CustomCronJobObject(reservation, node, OK);
                    } catch (Exception e) {
                        var status = switch (e) {
                            case HttpClientErrorException.BadRequest ignored1 -> BAD_REQUEST;
                            case HttpClientErrorException.NotFound ignored2 -> NOT_FOUND;
                            case HttpClientErrorException.Forbidden ignored3 -> FORBIDDEN;
                            case HttpClientErrorException.Unauthorized ignored4 -> UNAUTHORIZED;
                            default -> INTERNAL_SERVER_ERROR;
                        };

                        log.error("Status is %s \nMessage %s".formatted(status, e.getMessage()));

                        return new CustomCronJobObject(reservation, null, status);
                    }
                })
                .toList();

        return CustomUtil.asynchronousTasks(futures, CronJob.class)
                .thenApply(c -> c.stream().map(Supplier::get).toList())
                .join();
    }

    /**
     * Filters successful {@link OrderReservation}.
     * */
    private boolean onSuccess(CustomCronJobObject obj) {
        return obj.status().equals(OK)
                && obj.node().get("message").textValue().equalsIgnoreCase("Verification successful")
                && obj.node().get("data").get("status").textValue().equalsIgnoreCase("success");
    }

}