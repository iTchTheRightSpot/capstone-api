package dev.webserver.payment.service;

import dev.webserver.cart.entity.CartItem;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.cart.repository.CartItemRepo;
import dev.webserver.checkout.CheckoutService;
import dev.webserver.checkout.CustomObject;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.OutOfStockException;
import dev.webserver.payment.entity.OrderReservation;
import dev.webserver.payment.projection.OrderReservationPojo;
import dev.webserver.payment.projection.RaceConditionCartPojo;
import dev.webserver.payment.repository.OrderReservationRepo;
import dev.webserver.payment.response.PaymentResponse;
import dev.webserver.product.entity.ProductSku;
import dev.webserver.product.repository.ProductSkuRepo;
import dev.webserver.shipping.entity.ShipSetting;
import dev.webserver.thirdparty.ThirdPartyPaymentService;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.webserver.enumeration.ReservationStatus.PENDING;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RaceConditionService {

    private static final Logger log = LoggerFactory.getLogger(RaceConditionService.class);

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
     * {@link CartItem}) from the inventory of corresponding {@link ProductSku} items.
     * It creates reservations for the items in the cart to ensure that they are not
     * oversold. The method also generates payment information based on the user's
     * country and selected currency, preparing the response for payment.
     *
     * @param req The HttpServletRequest passed from the PaymentController.
     * @param country The country of the user would like to ship to which corresponds
     *                to {@link ShipSetting}.
     * @param currency The currency selected for the payment, of type SarreCurrency.
     * @return A PaymentResponse containing payment details for the user.
     * @throws CustomNotFoundException If custom cookie does not contain in {@link HttpServletRequest},
     * the {@link ShoppingSession} is invalid, or {@link CartItem} is empty.
     * @throws OutOfStockException If {@link CartItem} quantity is greater {@link ProductSku} inventory.
     * @throws JpaSystemException if {@link ProductSku} property 'inventory' is negative.
     */
    public PaymentResponse raceCondition(
            final HttpServletRequest req,
            final String country,
            final SarreCurrency currency
    ) {
        final CustomObject obj = checkoutService
                .validateCurrentShoppingSession(req, country.toLowerCase().trim());

        final var reservations = reservationRepo
                .allPendingNoneExpiredReservationsAssociatedToShoppingSession(
                        obj.session().shoppingSessionId(),
                        CustomUtil.toUTC(new Date()),
                        PENDING
                );

        final String reference = UUID.randomUUID().toString();

        final long instant = Instant.now()
                .plus(bound, ChronoUnit.MINUTES)
                .toEpochMilli();
        final Date toExpire = CustomUtil.toUTC(new Date(instant));

        raceConditionImpl(reference, reservations, obj.cartItems(), toExpire, obj.session());

        final var list = this.cartItemRepo
                .amountToPayForAllCartItemsForShoppingSession(obj.session().shoppingSessionId(), currency);

        final BigDecimal total = CustomUtil
                .calculateTotal(
                        CustomUtil.cartItemsTotalAndTotalWeight(list).total(),
                        obj.tax().rate(),
                        currency.equals(SarreCurrency.USD)
                                ? obj.ship().usdPrice()
                                : obj.ship().ngnPrice()
                );

        // if ngn remove leading zeros
        final var secret = this.thirdPartyService.payStackCredentials();
        return new PaymentResponse(
                reference,
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
     * of corresponding {@link ProductSku} items. It creates {@link OrderReservation} for
     * the items in the cart to ensure that they are not oversold. If any inconsistency occurs,
     * such as inventory becoming negative, it throws an {@link OutOfStockException}.
     *
     * @param reservations A list of existing {@code OrderReservations} associated with
     *                     the {@link ShoppingSession}.
     * @param carts        A list of {@link CartItem} representing items in the user's cart.
     * @param toExpire     The expiration date for the reservations.
     * @param session      The {@link ShoppingSession} associated with the user's device.
     * @throws OutOfStockException If inventory becomes negative due to reservations.
     */
    void raceConditionImpl(
            final String reference,
            final List<OrderReservationPojo> reservations,
            final List<RaceConditionCartPojo> carts,
            final Date toExpire,
            final ShoppingSession session
    ) {
        try {
            if (reservations.isEmpty()) {
                for (var cart : carts) {
                    onCartItemQtyGreaterThanProductSkuInventory(cart);

                    this.productSkuRepo.updateProductSkuInventoryBySubtractingFromExistingInventory(
                            cart.getProductSkuSku(),
                            cart.getCartItemQty()
                    );
                    reservationRepo.saveOrderReservation(
                            reference,
                            cart.getCartItemQty(),
                            PENDING,
                            toExpire,
                            cart.getProductSkuId(),
                            cart.getShoppingSessionId()
                    );
                }
            } else {
                final Map<String, OrderReservationPojo> map = reservations.stream()
                        .collect(Collectors.toMap(OrderReservationPojo::getProductSkuSku, pojo -> pojo));
                onPendingReservationsNotEmpty(
                        reference,
                        session,
                        toExpire,
                        map,
                        carts
                );
            }
        } catch (OutOfStockException e) {
            log.error(e.getMessage());
            throw new OutOfStockException(e.getMessage());
        } catch (JpaSystemException e) {
            log.error(e.getMessage());
            throw new OutOfStockException("an item in your cart is out of stock");
        }
    }

    private void onCartItemQtyGreaterThanProductSkuInventory(RaceConditionCartPojo cart) {
        if (cart.getCartItemQty() > cart.getProductSkuInventory()) {
            final var optional = productSkuRepo
                    .productByProductSku(cart.getProductSkuSku());

            final String name = optional.isPresent() ? optional.get().getName() : "";

            throw new OutOfStockException("%s %s is out of stock"
                    .formatted(name, cart.getProductSkuSize()));
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
     * @param reference a unique property for
     * @param session The {@link ShoppingSession} associated with the user's
     *                device.
     * @param toExpire The expiration date for the reservations.
     * @param map A map of existing {@link OrderReservation} indexed by
     *            {@link ProductSku} property sku.
     * @param cartItems A list of {@link CartItem} representing items in the
     *                  user's cart.
     * @throws OutOfStockException if {@link CartItem} property qty is greater
     * than {@code ProductSku} property inventory.
     * @throws JpaSystemException if {@link ProductSku} property 'inventory' is negative.
     * */
    void onPendingReservationsNotEmpty(
            final String reference,
            final ShoppingSession session,
            final Date toExpire,
            final Map<String, OrderReservationPojo> map,
            final List<RaceConditionCartPojo> cartItems
    ) {
        for (var cart : cartItems) {
            onCartItemQtyGreaterThanProductSkuInventory(cart);

            if (map.containsKey(cart.getProductSkuSku())) {
                final OrderReservationPojo reservation = map.get(cart.getProductSkuSku());

                if (cart.getCartItemQty() > reservation.getReservationQty()) {
                    this.reservationRepo
                            .deductFromProductSkuInventoryAndReplaceReservationQty(
                                    cart.getCartItemQty() - reservation.getReservationQty(),
                                    cart.getCartItemQty(),
                                    reference,
                                    toExpire,
                                    session.cookie(),
                                    cart.getProductSkuSku(),
                                    PENDING
                            );
                } else if (cart.getCartItemQty() < reservation.getReservationQty()) {
                    this.reservationRepo
                            .addToProductSkuInventoryAndReplaceReservationQty(
                                    reservation.getReservationQty() - cart.getCartItemQty(),
                                    cart.getCartItemQty(),
                                    reference,
                                    toExpire,
                                    session.cookie(),
                                    cart.getProductSkuSku(),
                                    PENDING
                            );
                }

                map.remove(cart.getProductSkuSku());
            } else {
                this.productSkuRepo.updateProductSkuInventoryBySubtractingFromExistingInventory(
                        cart.getProductSkuSku(),
                        cart.getCartItemQty()
                );
                reservationRepo.saveOrderReservation(
                        reference,
                        cart.getCartItemQty(),
                        PENDING,
                        toExpire,
                        cart.getProductSkuId(),
                        cart.getShoppingSessionId()
                );
            }
        }

        for (Map.Entry<String, OrderReservationPojo> entry : map.entrySet()) {
            final OrderReservationPojo reservation = entry.getValue();
            this.productSkuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                    reservation.getProductSkuSku(),
                    reservation.getReservationQty()
            );
            this.reservationRepo.deleteById(reservation.getReservationId());
        }
    }

}