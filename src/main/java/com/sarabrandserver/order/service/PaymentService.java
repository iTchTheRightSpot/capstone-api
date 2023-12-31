package com.sarabrandserver.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.order.entity.OrderReservation;
import com.sarabrandserver.order.repository.OrderReservationRepo;
import com.sarabrandserver.order.response.PaymentResponse;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final long bound = 15;

    @Value("${cart.cookie.name}")
    private String CART_COOKIE;
    @Value(value = "${cart.split}")
    private String SPLIT;

    private final ProductSkuRepo productSkuRepo;
    private final ObjectMapper objectMapper;
    private final CartItemRepo cartItemRepo;
    private final OrderReservationRepo reservationRepo;
    private final ThirdPartyPaymentService thirdPartyService;
    private final CustomUtil customUtil;

    /** TODO add shipping and taxes
     * Method helps prevent race condition or overselling by temporarily deducting
     * what is in the users cart with ProductSKU inventory.
     * @param req is passed from the controller
     * @param currency is currency customer wants to pay with
     * @throws SQLException if ProductSku inventory is negative
     * */
    @Transactional
    public PaymentResponse raceCondition(HttpServletRequest req, SarreCurrency currency) {
        Cookie cookie = this.customUtil.cookie.apply(req, CART_COOKIE);

        if (cookie == null) {
            throw new CustomNotFoundException("No cookie found. Kindly refresh window");
        }

        String sessionId = cookie.getValue().split(this.SPLIT)[0];

        var cartItems = this.cartItemRepo
                .cart_items_by_shopping_session_cookie(sessionId);

        if (cartItems.isEmpty()) {
            throw new CustomNotFoundException("invalid shopping session");
        }

        // retrieve all pending OrderReservation
        var reservations = this.reservationRepo
                .orderReservationByCookie(sessionId, PENDING);

        long toExpire = Instant.now().plus(bound, ChronoUnit.MINUTES).toEpochMilli();
        Date date = this.customUtil.toUTC(new Date(toExpire));

        if (reservations.isEmpty()) {
            for (CartItem c : cartItems) {
                this.productSkuRepo
                        .updateInventoryOnMakingReservation(c.getSku(), c.getQty());
                this.reservationRepo
                        .save(new OrderReservation(sessionId, c.getSku(), c.getQty(), PENDING, date));
            }
        } else {
            onPendingReservationsNotEmpty(sessionId, date, reservations, cartItems);
        }

        BigDecimal total = cartItemsTotal(sessionId, currency);
        var secret = this.thirdPartyService.payStackCredentials();
        return new PaymentResponse(secret.pubKey(), currency, total);
    }

    private BigDecimal cartItemsTotal(String sessionId, SarreCurrency currency) {
        return this.cartItemRepo
                .total_amount_in_default_currency(sessionId, currency)
                .stream()
                .map(pojo -> pojo.getPrice().multiply(BigDecimal.valueOf(pojo.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * The implementation below eliminates race condition for a situation where a user is indecisive about
     * the number of items to purchase.
     * 1. We convert reservations into a map of OrderReservation.sku to the object.
     *    Think map: { key: "OrderReservation.sku", value: the object }.
     * 2. Iterate cartItems for find CartItem.sku that contains in the map.
     * 3. Now that we have the items we want, we update ProductSku inventory and OrderReservation qty if
     *    we have a situation where the items in our CartItem qty is greater or less than OrderReservation qty.
     * 4. For cart.getQty() > reservation.getQty(), we subtract (cart.getQty() - reservation.getQty()) from
     *    ProductSku inv since the reservation already exists.
     * 5. For cart.getQty() < reservation.getQty(), we add (reservation.getQty() - cart.getQty()) from
     *    ProductSku inv since the reservation already exists.
     * 6. For step 4 and 5, we update OrderReservation qty to CartItem qty
     * 7. Final step in the iteration of cartItems is to remove the sku that contain from the map
     * 8. Finally, after the current data have been purged, we delete unassociated data.
     *
     * @param date The current date.
     * @param reservations List of {@code OrderReservation} objects representing pending reservations.
     * @param cartItems List of {@code CartItem} objects representing items in the shopping cart.
     * @param sessionId The ShoppingSession cookie associated with the cart items.
     * */
    private void onPendingReservationsNotEmpty(
            String sessionId,
            Date date,
            List<OrderReservation> reservations,
            List<CartItem> cartItems
    ) {
        Map<String, OrderReservation> map = reservations.stream()
                .collect(Collectors.toMap(OrderReservation::getSku, orderReservation -> orderReservation));

        for (CartItem cart : cartItems) {
            if (map.containsKey(cart.getSku())) {
                OrderReservation reservation = map.get(cart.getSku());

                if (cart.getQty() > reservation.getQty()) {
                    this.reservationRepo
                            .onSub(
                                    cart.getQty() - reservation.getQty(),
                                    cart.getQty(),
                                    date,
                                    sessionId,
                                    cart.getSku(),
                                    PENDING
                            );
                } else if (cart.getQty() < reservation.getQty()) {
                    this.reservationRepo
                            .onAdd(
                                    reservation.getQty() - cart.getQty(),
                                    cart.getQty(),
                                    date,
                                    sessionId,
                                    cart.getSku(),
                                    PENDING
                            );
                }

                map.remove(cart.getSku());
            } else {
                this.productSkuRepo
                        .updateInventoryOnMakingReservation(cart.getSku(), cart.getQty());
                this.reservationRepo
                        .save(new OrderReservation(sessionId, cart.getSku(), cart.getQty(), PENDING, date));
            }
        }

        for (Map.Entry<String, OrderReservation> entry : map.entrySet()) {
            OrderReservation r = entry.getValue();
            this.productSkuRepo
                    .updateInventory(r.getSku(), r.getQty());
            this.reservationRepo.deleteOrderReservationByReservationId(r.getReservationId());
        }
    }

    /**
     * Method retrieves info sent from Flutterwave via webhook
     * */
    @Transactional
    public void order(HttpServletRequest req) {
        String body = null;
        try {
            body = requestBody(req);
            log.info("Request body Paystack {}", body);
        } catch (IOException e) {
            log.error("Error retrieving body from HttpServletRequest {}", e.getMessage());
        }

        if (body == null) return;

        var secret = this.thirdPartyService.payStackCredentials();
        String header = req.getHeader("x-paystack-signature");
        String validate = validateRequestFromPayStack(secret.secretKey(), body);

        log.info("Validate request came from Paystack {}", validate);

        if (!validate.toLowerCase().equals(header)) {
            return;
        }

        // TODO verify payment came from paystack
        // https://paystack.com/docs/payments/verify-payments/
        // TODO update order reservation table
        // TODO save to PaymentDetail, OrderDetail and Address
    }

    /**
     * Transforms request body into a string
     * */
    private String requestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Validates if request came from paystack
     * <a href="https://paystack.com/docs/payments/webhooks/">...</a>
     * */
    private String validateRequestFromPayStack(String secretKey, String body) {
        String hmac = "HmacSHA512";
        try {
            JsonNode node = this.objectMapper.readValue(body, JsonNode.class);
            Mac sha512_HMAC = Mac.getInstance(hmac);
            sha512_HMAC.init(new SecretKeySpec(secretKey.getBytes(UTF_8), hmac));
            return DatatypeConverter
                    .printHexBinary(sha512_HMAC.doFinal(node.toString().getBytes(UTF_8)));
        } catch (Exception e) {
            log.info("Error validating request from paystack {}", e.getMessage());
            return "";
        }
    }

    /**
     * Returning pending order reservation back to stock.
     * Note we are making the interval to be 20 mins
     * for extra breathing space from payment provider webhook
     * */
    @Scheduled(fixedRate = bound + 5, timeUnit = TimeUnit.MINUTES, zone = "UTC")
    public void schedule() {
        scheduledDelete();
    }

    void scheduledDelete() {
        Date date = this.customUtil.toUTC(new Date());
        var list = this.reservationRepo
                .allPendingExpiredReservations(date, PENDING);

        for (OrderReservation r : list) {
            this.productSkuRepo
                    .updateInventory(r.getSku(), r.getQty());
            this.reservationRepo
                    .deleteOrderReservationByReservationId(r.getReservationId());
        }
        this.reservationRepo.deleteExpired(date);
    }

}