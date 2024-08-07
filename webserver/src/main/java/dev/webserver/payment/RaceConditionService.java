package dev.webserver.payment;

import dev.webserver.cart.Cart;
import dev.webserver.cart.ICartRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.OutOfStockException;
import dev.webserver.external.payment.ThirdPartyPaymentService;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
import dev.webserver.shipping.ShipSetting;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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

    private final ProductSkuRepository productSkuRepository;
    private final ICartRepository cartRepository;
    private final OrderReservationRepository reservationRepository;
    private final ThirdPartyPaymentService thirdPartyService;
    private final CheckoutService checkoutService;

    /**
     * Prevents race conditions or overselling by temporarily reserving inventory
     * based on the items in the user's cart.
     * <p>
     * This method is used to prevent race conditions or overselling scenarios by
     * temporarily deducting the quantity in a users cart (represented as
     * {@link Cart}) from the inventory of corresponding {@link ProductSku} items.
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
     * the {@link ShoppingSession} is invalid, or {@link Cart} is empty.
     * @throws OutOfStockException If {@link Cart} quantity is greater {@link ProductSku} inventory.
     * @throws RuntimeException if {@link ProductSku} property 'inventory' is negative.
     */
    public PaymentResponse raceCondition(
            final HttpServletRequest req,
            final String country,
            final SarreCurrency currency
    ) {
        final CustomCheckoutObject obj = checkoutService
                .validateCurrentShoppingSession(req, country.toLowerCase().trim());

        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var reservations = reservationRepository
                .allPendingNoneExpiredReservationsAssociatedToShoppingSession(
                        obj.session().sessionId(),
                        ldt,
                        PENDING
                );

        final String reference = UUID.randomUUID().toString();

        raceConditionImpl(reference, reservations, obj.cartItems(), ldt.plusMinutes(bound), obj.session());

        final var list = cartRepository
                .amountToPayForAllCartItemsForShoppingSession(obj.session().sessionId(), currency);

        final BigDecimal total = CustomUtil
                .calculateTotal(
                        CustomUtil.cartItemsTotalAndTotalWeight(list).total(),
                        obj.tax().rate(),
                        currency.equals(SarreCurrency.USD)
                                ? obj.ship().usdPrice()
                                : obj.ship().ngnPrice()
                );

        // if ngn remove leading zeros
        final var secret = thirdPartyService.payStackCredentials();
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
     * @param carts        A list of {@link Cart} representing items in the user's cart.
     * @param toExpire     The expiration date for the reservations.
     * @param session      The {@link ShoppingSession} associated with the user's device.
     * @throws OutOfStockException If inventory becomes negative due to reservations.
     */
    void raceConditionImpl(
            final String reference,
            final List<OrderReservationDbMapper> reservations,
            final List<RaceConditionCartDbMapper> carts,
            final LocalDateTime toExpire,
            final ShoppingSession session
    ) {
        try {
            if (reservations.isEmpty()) {
                for (var cart : carts) {
                    onCartItemQtyGreaterThanProductSkuInventory(cart);

                    productSkuRepository
                            .updateProductSkuInventoryBySubtractingFromExistingInventory(cart.sku(), cart.qty());
                    reservationRepository.saveOrderReservation(
                            reference,
                            cart.qty(),
                            PENDING,
                            toExpire,
                            cart.skuId(),
                            cart.sessionId()
                    );
                }
            } else {
                final Map<String, OrderReservationDbMapper> map = reservations.stream()
                        .collect(Collectors.toMap(OrderReservationDbMapper::sku, pojo -> pojo));
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
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw new OutOfStockException("an item in your cart is out of stock");
        }
    }

    private void onCartItemQtyGreaterThanProductSkuInventory(final RaceConditionCartDbMapper cart) {
        if (cart.qty() > cart.inventory()) {
            final var optional = productSkuRepository.productByProductSku(cart.sku());

            final String name = optional.isPresent() ? optional.get().name() : "";

            throw new OutOfStockException("%s %s is out of stock".formatted(name, cart.size()));
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
     * @param reservations A map of existing {@link OrderReservation} indexed by
     *            {@link ProductSku} property sku.
     * @param cartItems A list of {@link Cart} representing items in the
     *                  user's cart.
     * @throws OutOfStockException if {@link Cart} property qty is greater
     * than {@code ProductSku} property inventory.
     * */
    void onPendingReservationsNotEmpty(
            final String reference,
            final ShoppingSession session,
            final LocalDateTime toExpire,
            final Map<String, OrderReservationDbMapper> reservations,
            final List<RaceConditionCartDbMapper> cartItems
    ) {
        for (final var cart : cartItems) {
            onCartItemQtyGreaterThanProductSkuInventory(cart);

            if (reservations.containsKey(cart.sku())) {
                final OrderReservationDbMapper reservation = reservations.get(cart.sku());

                if (cart.qty() > reservation.qty()) {
                    reservationRepository
                            .deductFromProductSkuInventoryAndReplaceReservationQty(
                                    cart.qty() - reservation.qty(),
                                    cart.qty(),
                                    reference,
                                    toExpire,
                                    session.cookie(),
                                    cart.sku(),
                                    PENDING
                            );
                } else if (cart.qty() < reservation.qty()) {
                    reservationRepository
                            .addToProductSkuInventoryAndReplaceReservationQty(
                                    reservation.qty() - cart.qty(),
                                    cart.qty(),
                                    reference,
                                    toExpire,
                                    session.cookie(),
                                    cart.sku(),
                                    PENDING
                            );
                }

                reservations.remove(cart.sku());
            } else {
                productSkuRepository
                        .updateProductSkuInventoryBySubtractingFromExistingInventory(cart.sku(), cart.qty());
                reservationRepository.saveOrderReservation(
                        reference,
                        cart.qty(),
                        PENDING,
                        toExpire,
                        cart.skuId(),
                        cart.sessionId()
                );
            }
        }

        for (final Map.Entry<String, OrderReservationDbMapper> entry : reservations.entrySet()) {
            final OrderReservationDbMapper reservation = entry.getValue();
            productSkuRepository
                    .updateProductSkuInventoryByAddingToExistingInventory(reservation.sku(), reservation.qty());
            reservationRepository.deleteById(reservation.reservationId());
        }
    }

}