package com.sarabrandserver.cron;

import com.fasterxml.jackson.databind.JsonNode;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.exception.CustomServerError;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import com.sarabrandserver.util.CustomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;

@Component
class CronJobs {

    private static final Logger log = LoggerFactory.getLogger(CronJobs.class);

    private final RestClient restClient;
    private final ProductSkuRepo skuRepo;
    private final OrderReservationRepo reservationRepo;
    private final ShoppingSessionRepo sessionRepo;
    private final CartItemRepo cartItemRepo;
    private final String secretKey;

    public CronJobs(
            RestClient.Builder clientBuilder,
            ProductSkuRepo skuRepo,
            OrderReservationRepo reservationRepo,
            ShoppingSessionRepo sessionRepo,
            CartItemRepo cartItemRepo,
            ThirdPartyPaymentService paymentService
    ) {
        this.skuRepo = skuRepo;
        this.reservationRepo = reservationRepo;
        this.sessionRepo = sessionRepo;
        this.cartItemRepo = cartItemRepo;
        this.secretKey = paymentService.payStackCredentials().secretKey();
        this.restClient = clientBuilder.build();
    }

    /**
     * Cron job to run every 15 mins.
     * @see <a href="https://docs.spring.io/spring-framework/reference/integration/scheduling.html">documentation</a>
     * */
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES, zone = "UTC")
    @Transactional
    public void schedule() {
        log.info("starting cron job");
        onDeleteShoppingSessions();
        onDeleteOrderReservations();
        log.info("end of cron job");
    }

    /**
     * Deletes all expired {@link ShoppingSession} instances and their associated {@link CartItem}.
     * This method retrieves all expired {@link ShoppingSession} instances from the repository
     * using the current date as a reference point. For each expired session, it deletes all
     * {@link CartItem} associated with that session and then deletes the session itself.
     */
    public void onDeleteShoppingSessions() {
        sessionRepo.allExpiredShoppingSession(CustomUtil.toUTC(new Date()))
                .forEach(session -> {
                    this.cartItemRepo.deleteCartItemsByShoppingSessionId(
                            session.shoppingSessionId());
                    this.sessionRepo.deleteById(session.shoppingSessionId());
                });
    }

    /**
     * Delete {@link OrderReservation}s.
     * */
    @Transactional(rollbackFor = CustomServerError.class)
    public void onDeleteOrderReservations() {
        Date date = CustomUtil
                .toUTC(Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)));

        validateOrderReservationsFromPaystack(reservationRepo
                .allPendingExpiredReservations(date, PENDING))
                .forEach(reservation -> {
                    skuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                            reservation.getProductSku().getSku(), reservation.getQty());
                    reservationRepo.deleteOrderReservationByReservationId(
                                    reservation.getReservationId());
                });
    }

    /**
     * Finds failed {@link OrderReservation} from Paystack based on list of expired
     * {@link OrderReservation} objects.
     * <p>
     * Retrieves the status of transactions from Paystack and filters out reservations with
     * transaction references that do not exist or have statuses of abandoned or failed.
     *
     * @param reservations The list of expired {@link OrderReservation} objects to validate.
     * @return A list of {@link OrderReservation} objects that have failed transactions.
     * @throws CustomServerError if an error occurs when asynchronous validating references
     *                           from Paystack.
     * @see <a href="https://paystack.com/docs/payments/verify-payments/">Paystack Documentation</a>
     */
    public List<OrderReservation> validateOrderReservationsFromPaystack(
            List<OrderReservation> reservations
    ) {
        record CustomCronObject(OrderReservation reservation, JsonNode node) {
        }

        // create tasks
        var futures = reservations.stream()
                .map(reservation -> (Supplier<CustomCronObject>) () -> {
                    URI uri = UriComponentsBuilder
                            .fromUriString("https://api.paystack.co/transaction/verify")
                            .pathSegment(reservation.getReference())
                            .build()
                            .toUri();

                    JsonNode node = restClient.get().uri(uri)
                            .header("Authorization",
                                    "Bearer %s".formatted(secretKey)
                            )
                            .retrieve().body(JsonNode.class);
                    return new CustomCronObject(reservation, node);
                })
                .toList();

        return CustomUtil.asynchronousTasks(futures, CronJobs.class)
                .exceptionally(e -> null)
                .thenApply(f -> f.stream()
                        .map(supplier -> Optional.ofNullable(supplier.get()))
                        .filter(Optional::isPresent)
                        .filter(obj -> isFailedReservation(obj.get().node()))
                        .map(obj -> obj.get().reservation())
                        .toList()
                )
                .join();
    }

    /**
     * Checks if the provided {@link JsonNode} represents a failed {@link OrderReservation}
     * based on the Paystack response.
     *
     * <p>
     * This method examines the Paystack response contained in the provided
     * {@link JsonNode} to determine if the corresponding reservation has a failed
     * transaction status. It checks if the response message indicates a transaction
     * reference not found or a verification success with a transaction status of abandoned
     * or failed.
     *
     * @param node The {@code JsonNode} containing the Paystack response for the reservation.
     * @return {@code true} if the reservation has a failed transaction status,
     * {@code false} otherwise.
     */
    private boolean isFailedReservation(JsonNode node) {
        String message = node.get("message").textValue();
        String status = node.get("data").get("status").textValue();

        // Check if transaction reference not found or status is abandoned/failed
        return message.equalsIgnoreCase("Transaction reference not found") ||
                (message.equalsIgnoreCase("Verification successful") &&
                        (status.equalsIgnoreCase("abandoned")
                                || status.equalsIgnoreCase("failed")));
    }

}
