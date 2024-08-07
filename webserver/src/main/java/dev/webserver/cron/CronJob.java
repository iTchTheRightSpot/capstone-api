package dev.webserver.cron;

import com.fasterxml.jackson.databind.JsonNode;
import dev.webserver.cart.Cart;
import dev.webserver.cart.ICartRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.cart.IShoppingSessionRepository;
import dev.webserver.external.log.ILogEventPublisher;
import dev.webserver.external.payment.ThirdPartyPaymentService;
import dev.webserver.payment.OrderReservation;
import dev.webserver.payment.OrderReservationRepository;
import dev.webserver.payment.PaymentDetailService;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
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
    private final ProductSkuRepository skuRepo;
    private final OrderReservationRepository reservationRepo;
    private final IShoppingSessionRepository sessionRepo;
    private final ICartRepository ICartRepository;
    private final PaymentDetailService paymentDetailService;
    private final String secretKey;
    private final ILogEventPublisher publisher;

    public CronJob(
            RestClient.Builder clientBuilder,
            ProductSkuRepository skuRepo,
            OrderReservationRepository reservationRepo,
            IShoppingSessionRepository sessionRepo,
            ICartRepository ICartRepository,
            PaymentDetailService paymentDetailService,
            ThirdPartyPaymentService paymentService,
            ILogEventPublisher publisher
    ) {
        this.skuRepo = skuRepo;
        this.reservationRepo = reservationRepo;
        this.sessionRepo = sessionRepo;
        this.ICartRepository = ICartRepository;
        this.paymentDetailService = paymentDetailService;
        this.secretKey = paymentService.payStackCredentials().secretKey();
        this.restClient = clientBuilder.build();
        this.publisher = publisher;
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
     * Deletes all expired {@link ShoppingSession} instances and their associated {@link Cart}.
     * This method retrieves all expired {@link ShoppingSession} instances from the repository
     * using the current date as a reference point. For each expired session, it deletes all
     * {@link Cart} associated with that session and then deletes the session itself.
     */
    @Transactional(rollbackFor = Exception.class)
    public void onDeleteShoppingSessions() {
        sessionRepo.allExpiredShoppingSession(CustomUtil.toUTC(new Date()))
                .forEach(session -> {
                    this.ICartRepository.deleteCartByShoppingSessionId(
                            session.sessionId());
                    this.sessionRepo.deleteById(session.sessionId());
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
                .filter(reservation -> onSuccess(reservation) || reservation.status().equals(BAD_REQUEST) || reservation.status().equals(NOT_FOUND))
                .forEach(obj -> {
                    if (obj.status().equals(OK)) {
                        JsonNode data = obj.node().get("data");
                        JsonNode metadata = data.get("metadata");
                        String email = metadata.get("email").asText();
                        String reference = data.get("reference").textValue();

                        if (!paymentDetailService.paymentDetailExists(email, reference)) {
                            paymentDetailService.onSuccessfulPayment(data);
                            publisher.publishPurchase(metadata.get("name").asText(), email);
                        }
                    }

                    skuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                            obj.reservation().getSkuId().getSku(),
                            obj.reservation().getQty()
                    );

                    reservationRepo.deleteById(obj.reservation().getReservationId());
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

        return CustomUtil.asynchronousTasks(futures).join();
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