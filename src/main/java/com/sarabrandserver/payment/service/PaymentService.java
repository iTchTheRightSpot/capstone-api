package com.sarabrandserver.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.payment.response.PaymentResponse;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.shipping.entity.Shipping;
import com.sarabrandserver.shipping.service.ShippingService;
import com.sarabrandserver.tax.Tax;
import com.sarabrandserver.tax.TaxService;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
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
    @Value("${cart.cookie.name}")
    private String CART_COOKIE;
    @Setter
    @Value(value = "${cart.split}")
    private String SPLIT;
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
    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final OrderReservationRepo reservationRepo;
    private final ThirdPartyPaymentService thirdPartyService;
    private final ShippingService shippingService;
    private final TaxService taxService;

    /**
     * Method helps prevent race condition or overselling by temporarily deducting
     * what is in the users cart with ProductSKU inventory.
     *
     * @param req is passed from the {@code PaymentController}
     * @param currency is is of {@code SarreCurrency}.
     * @param country tells if user should be charged international or local fees.
     * @throws OutOfStockException if ProductSku inventory is negative
     * @throws CustomNotFoundException if {@code Shipping} object is not found.
     * */
    @Transactional
    public PaymentResponse raceCondition(
            HttpServletRequest req,
            final String country,
            final SarreCurrency currency
    ) {
        Cookie cookie = CustomUtil.cookie(req, CART_COOKIE);

        if (cookie == null) {
            throw new CustomNotFoundException("no cookie found. kindly refresh window");
        }

        var optionalShoppingSession = shoppingSessionRepo
                .shoppingSessionByCookie(cookie.getValue().split(this.SPLIT)[0]);

        if (optionalShoppingSession.isEmpty()) {
            throw new CustomNotFoundException("invalid shopping session");
        }

        ShoppingSession session = optionalShoppingSession.get();
        long sessionId = session.shoppingSessionId();

        var carts = cartItemRepo.cartItemsByShoppingSessionId(sessionId);

        if (carts.isEmpty()) {
            throw new CustomNotFoundException("cart is empty");
        }

        var reservations = this.reservationRepo
                .allPendingNoneExpiredReservationsAssociatedToShoppingSession(
                        sessionId,
                        CustomUtil.toUTC(new Date()),
                        PENDING
                );

        long instant = Instant.now()
                .plus(bound, ChronoUnit.MINUTES)
                .toEpochMilli();
        Date toExpire = CustomUtil.toUTC(new Date(instant));

        // race condition impl
        raceConditionImpl(reservations, carts, toExpire, session);

        Shipping shipping = shippingService
                .shippingByCountryElseReturnDefault(country);

        Tax tax = taxService.taxById(1);

        BigDecimal total = calculateTotal(
                cartItemsTotal(sessionId, currency),
                tax.percentage(),
                currency.equals(SarreCurrency.USD)
                        ? shipping.usdPrice()
                        : shipping.ngnPrice()
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

    BigDecimal calculateTotal(BigDecimal cartItemsTotal, double tax, BigDecimal shippingPrice) {
        var newTax = cartItemsTotal.multiply(BigDecimal.valueOf(tax));
        return cartItemsTotal
                .add(newTax)
                .add(shippingPrice);
    }

    /**
     * Calculates the total for each item. Total = weight + (price * qty)
     * */
    private BigDecimal cartItemsTotal(long sessionId, SarreCurrency currency) {
        return this.cartItemRepo
                .totalPojoByShoppingSessionId(sessionId, currency)
                .stream()
                .map(p -> {
                    BigDecimal mul = p.getPrice().multiply(BigDecimal.valueOf(p.getQty()));
                    return BigDecimal.valueOf(p.getWeight()).add(mul);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * The implementation below is the last line of defence to protect against
     * race condition/overselling. It applies the logic of:
     * 1. Error thrown if qty in a users cart is greater than what is in stock.
     * 2. A user reduces or adds a new {@code ProductSku} to their cart.
     * 3. Combination of step 2 but the qty for each {@code CartItem} is
     * might be different to what is reserved.
     * 4. User tries checking out with some items removed from cart.
     *
     * @param toExpire the current toExpire.
     * @param map of {@code Map<String, OrderReservation>} where the key is
     *            a unique string assigned to a {@code ProductSku} and
     *            the value is {@code OrderReservation}.
     * @param cartItems is a list of {@code CartItem} objects representing
     *                  items in the shopping cart.
     * @param session is a unique string of {@code ShoppingSession} assigned
     *                to every device that visits our application.
     * @throws OutOfStockException if {@code CartItem} property qty is
     * greater than {@code ProductSku} inventory.
     * @throws org.springframework.orm.jpa.JpaSystemException if
     * {@code ProductSku} is less than 0.
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