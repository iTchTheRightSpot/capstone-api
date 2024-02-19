package com.sarabrandserver.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.checkout.CheckoutService;
import com.sarabrandserver.checkout.CustomObject;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.projection.TotalPojo;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.payment.response.PaymentResponse;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Setter
    @Value("${sarre.usd.to.cent}")
    private String usdConversion;
    @Setter
    @Value("${sarre.ngn.to.kobo}")
    private String ngnConversion;
    @Setter
    @Value("${race-condition.expiration.bound}")
    private long bound;

    private final ProductSkuRepo productSkuRepo;
    private final CartItemRepo cartItemRepo;
    private final OrderReservationRepo reservationRepo;
    private final ThirdPartyPaymentService thirdPartyService;
    private final CheckoutService checkoutService;

    /**
     * Prevents race conditions or overselling by temporarily reserving inventory
     * based on the items in the user's cart.
     * <p>
     * This method is used to prevent race conditions or overselling scenarios by
     * temporarily deducting the quantity in a users cart (represented as
     * {@code CartItem}) from the inventory of corresponding {@code ProductSku} items.
     * It creates reservations for the items in the cart to ensure that they are not
     * oversold. The method also generates payment information based on the user's
     * country and selected currency, preparing the response for payment.
     *
     * @param req The HttpServletRequest passed from the PaymentController.
     * @param country The country of the user would like to ship to which corresponds
     *                to {@code Shipping}.
     * @param currency The currency selected for the payment, of type SarreCurrency.
     * @return A PaymentResponse containing payment details for the user.
     * @throws OutOfStockException If the inventory of a ProductSku becomes negative after
     * reservation.
     * @throws CustomNotFoundException If required information for checkout cannot be retrieved.
     */
    @Transactional
    public PaymentResponse raceCondition(
            HttpServletRequest req,
            final String country,
            final SarreCurrency currency
    ) {
        CustomObject obj = checkoutService
                .createCustomObjectForShoppingSession(req, country.toLowerCase().trim());

        var reservations = this.reservationRepo
                .allPendingNoneExpiredReservationsAssociatedToShoppingSession(
                        obj.session().shoppingSessionId(),
                        CustomUtil.toUTC(new Date()),
                        PENDING
                );

        long instant = Instant.now()
                .plus(bound, ChronoUnit.MINUTES)
                .toEpochMilli();
        Date toExpire = CustomUtil.toUTC(new Date(instant));

        raceConditionImpl(reservations, obj.cartItems(), toExpire, obj.session());

        List<TotalPojo> list = this.cartItemRepo
                .totalPojoByShoppingSessionId(obj.session().shoppingSessionId(), currency);

        BigDecimal total = CustomUtil
                .calculateTotal(
                        CustomUtil.cartItemsTotalAndTotalWeight(list).total(),
                        obj.tax().rate(),
                        currency.equals(SarreCurrency.USD)
                                ? obj.ship().usdPrice()
                                : obj.ship().ngnPrice()
                );

        var secret = this.thirdPartyService.payStackCredentials();
        return new PaymentResponse(
                secret.pubKey(),
                currency,
                CustomUtil.convertCurrency(
                        currency.equals(SarreCurrency.NGN) ? ngnConversion : usdConversion,
                        currency,
                        total
                )
        );
    }

    /**
     * An implementation of core logic of raceCondition method above where
     * race conditions or overselling is prevented by temporarily reserving inventory
     * and updating order reservations for items in the user's cart.
     * <p>
     * This method ensures data consistency and prevents race conditions or overselling
     * by temporarily deducting the quantity of items in the user's cart from the inventory
     * of corresponding {@code ProductSku} items. It creates {@code OrderReservations} for
     * the items in the cart to ensure that they are not oversold. If any inconsistency occurs,
     * such as inventory becoming negative, it throws an OutOfStockException.
     *
     * @param reservations A list of existing {@code OrderReservations} associated with
     *                     the {@code ShoppingSession}.
     * @param carts        A list of {@code CartItem} representing items in the user's cart.
     * @param toExpire     The expiration date for the reservations.
     * @param session      The {@code ShoppingSession} associated with the user's device.
     * @throws OutOfStockException If inventory becomes negative due to reservations.
     */
    @Transactional
    void raceConditionImpl(
            List<OrderReservation> reservations,
            List<CartItem> carts,
            Date toExpire,
            ShoppingSession session
    ) {
        try {
            if (reservations.isEmpty()) {
                for (CartItem cart : carts) {
                    if (cart.quantityIsGreaterThanProductSkuInventory()) {
                        throw new OutOfStockException("an item in your cart is out of stock");
                    }

                    this.productSkuRepo
                            .updateProductSkuInventoryBySubtractingFromExistingInventory(
                                    cart.getProductSku().getSku(),
                                    cart.getQty()
                            );
                    this.reservationRepo
                            .save(new OrderReservation(
                                            cart.getQty(),
                                            PENDING,
                                    toExpire,
                                            cart.getProductSku(),
                                            session
                                    )
                            );
                }
            } else {
                Map<String, OrderReservation> map = reservations
                        .stream()
                        .collect(Collectors.toMap(
                                reservation -> reservation.getProductSku().getSku(),
                                orderReservation -> orderReservation)
                        );
                onPendingReservationsNotEmpty(
                        session,
                        toExpire,
                        map,
                        carts
                );
            }
        } catch (RuntimeException e) {
            log.error("race condition exception thrown. {}", e.getMessage());
            throw new OutOfStockException("an item in your cart is out of stock");
        }
    }

    /**
     * Handles scenarios when pending reservations exist for items in the user's cart.
     * <p>
     * This method serves as the last line of defense against race conditions
     * or overselling by updating existing reservations and ensuring consistency
     * between cart items and order reservations. It applies the logic of
     * adjusting inventory quantities and replacing reservation quantities based
     * on the current state of the cart items and existing reservations.
     *
     * @param session The {@code ShoppingSession} associated with the user's
     *                device.
     * @param toExpire The expiration date for the reservations.
     * @param map A map of existing {@code OrderReservations} indexed by
     *            {@code ProductSku} property sku.
     * @param cartItems A list of {@code CartItem} representing items in the
     *                  user's cart.
     * @throws OutOfStockException if {@code CartItem} property qty is greater
     * than {@code ProductSku} property inventory.
     * @throws org.springframework.orm.jpa.JpaSystemException if {@code ProductSku}
     * property inventory becomes.
     * */
    @Transactional
    void onPendingReservationsNotEmpty(
            ShoppingSession session,
            Date toExpire,
            Map<String, OrderReservation> map,
            List<CartItem> cartItems
    ) {
        for (CartItem cart : cartItems) {
            if (cart.quantityIsGreaterThanProductSkuInventory()) {
                throw new OutOfStockException("");
            }

            if (map.containsKey(cart.getProductSku().getSku())) {
                OrderReservation reservation = map.get(cart.getProductSku().getSku());

                if (cart.getQty() > reservation.getQty()) {
                    this.reservationRepo
                            .deductFromProductSkuInventoryAndReplaceReservationQty(
                                    cart.getQty() - reservation.getQty(),
                                    cart.getQty(),
                                    toExpire,
                                    session.cookie(),
                                    cart.getProductSku().getSku(),
                                    PENDING
                            );
                } else if (cart.getQty() < reservation.getQty()) {
                    this.reservationRepo
                            .addToProductSkuInventoryAndReplaceReservationQty(
                                    reservation.getQty() - cart.getQty(),
                                    cart.getQty(),
                                    toExpire,
                                    session.cookie(),
                                    cart.getProductSku().getSku(),
                                    PENDING
                            );
                }

                map.remove(cart.getProductSku().getSku());
            } else {
                this.productSkuRepo
                        .updateProductSkuInventoryBySubtractingFromExistingInventory(
                                cart.getProductSku().getSku(),
                                cart.getQty()
                        );
                this.reservationRepo
                        .save(new OrderReservation(cart.getQty(), PENDING, toExpire, cart.getProductSku(), session));
            }
        }

        for (Map.Entry<String, OrderReservation> entry : map.entrySet()) {
            OrderReservation value = entry.getValue();
            this.productSkuRepo
                    .updateProductSkuInventoryByAddingToExistingInventory(
                            value.getProductSku().getSku(),
                            value.getQty()
                    );
            this.reservationRepo.deleteOrderReservationByReservationId(value.getReservationId());
        }
    }

    /**
     * method retrieves info sent from Flutterwave via webhook
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
            JsonNode node = new ObjectMapper().readValue(body, JsonNode.class);
            Mac sha512_HMAC = Mac.getInstance(hmac);
            sha512_HMAC.init(new SecretKeySpec(secretKey.getBytes(UTF_8), hmac));
            return DatatypeConverter
                    .printHexBinary(sha512_HMAC.doFinal(node.toString().getBytes(UTF_8)));
        } catch (Exception e) {
            log.info("Error validating request from paystack {}", e.getMessage());
            return "";
        }
    }

}