package dev.webserver.payment;

import dev.webserver.cart.Cart;
import dev.webserver.cart.ICartRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.cart.IShoppingSessionRepository;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.shipping.ShipSetting;
import dev.webserver.shipping.ShippingService;
import dev.webserver.tax.Tax;
import dev.webserver.tax.TaxService;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class CheckoutService {

    @Setter
    @Value("${cart.cookie.name}")
    private String cartcookie;
    @Setter
    @Value(value = "${cart.split}")
    private String split;

    private final ShippingService shippingService;
    private final TaxService taxService;
    private final IShoppingSessionRepository IShoppingSessionRepository;
    private final ICartRepository ICartRepository;

    /**
     * Generates checkout information based on a user's country and selected currency.
     * <p>
     * This method processes the user's checkout request by retrieving necessary information
     * such as the {@link ShoppingSession}, {@link Cart}, {@link ShipSetting},
     * and {@link Tax}. It then calculates the total amount for the checkout and constructs
     * a {@link Checkout} object containing the shipping price, tax information, and total
     * amount in the users choice of currency.
     *
     * @param req      The {@link HttpServletRequest} representing the user's request.
     * @param country  The country entered by the user during checkout.
     * @param currency The currency selected by the user for checkout.
     * @return A {@link Checkout} object containing objects needed to be sent to the UI.
     * @throws CustomNotFoundException If any required information is missing or invalid.
     */
    public Checkout checkout(final HttpServletRequest req, final String country, final SarreCurrency currency) {
        final CustomCheckoutObject obj = validateCurrentShoppingSession(req, country);

        final var list = this.ICartRepository
                .amountToPayForAllCartItemsForShoppingSession(obj.session().sessionId(), currency);

        final BigDecimal shipCost = currency.equals(SarreCurrency.USD)
                ? obj.ship().usdPrice()
                : obj.ship().ngnPrice();

        final CheckoutPair subtotal = CustomUtil.cartItemsTotalAndTotalWeight(list);

        final BigDecimal total = CustomUtil
                .calculateTotal(
                        subtotal.total(),
                        obj.tax().rate(),
                        shipCost
                );

        final var optional = Optional
                .ofNullable(SecurityContextHolder.getContext().getAuthentication());

        final String principal = optional.isEmpty() ? "" : switch (optional.get()) {
            case AnonymousAuthenticationToken ignored -> "";
            default -> optional.get().getName();
        };

        return new Checkout(
                principal,
                "%skg".formatted(subtotal.sumOfWeight()),
                shipCost,
                obj.tax().name(),
                obj.tax().rate(),
                total.subtract(total),
                subtotal.total(),
                total
        );
    }

    /**
     * Creates a {@link CustomCheckoutObject} based on the provided {@link HttpServletRequest} and
     * country.
     * <p>
     * This method retrieves custom cookie from the HttpServletRequest to find associated
     * {@link ShoppingSession} for the device. It then retrieves the associated {@link Cart}(s)
     * and checks if the cart is empty. Next, it retrieves the shipping information based
     * on the provided country. Finally, it retrieves the tax information. Using this
     * information, it constructs and returns a {@link CustomCheckoutObject} containing the
     * {@link ShoppingSession}, {@link Cart}, {@link ShipSetting}, and {@link Tax}.
     *
     * @param req     The HttpServletRequest containing the {@link ShoppingSession} cookie.
     * @param country The country for which {@link ShipSetting} information is retrieved.
     * @return A {@link CustomCheckoutObject} containing the {@link ShoppingSession}, {@link Cart}(s),
     * {@link ShipSetting}, and {@link Tax}.
     * @throws CustomNotFoundException If custom cookie does not contain in
     *                                 {@link HttpServletRequest}, the {@link ShoppingSession} is
     *                                 invalid, or {@link Cart} is empty.
     */
    public CustomCheckoutObject validateCurrentShoppingSession(final HttpServletRequest req, final String country) {
        final Cookie cookie = CustomUtil.cookie(req, cartcookie);

        if (cookie == null) {
            throw new CustomNotFoundException("no cookie found. kindly refresh window");
        }

        final var optional = IShoppingSessionRepository
                .shoppingSessionByCookie(cookie.getValue().split(split)[0]);

        if (optional.isEmpty()) {
            throw new CustomNotFoundException("invalid shopping session");
        }

        final ShoppingSession session = optional.get();

        final var carts = ICartRepository
                .cartByShoppingSessionId(session.sessionId());

        if (carts.isEmpty()) {
            throw new CustomNotFoundException("cart is empty");
        }

        final ShipSetting ship = shippingService
                .shippingByCountryElseReturnDefault(country);

        final Tax tax = taxService.taxById(1);

        return new CustomCheckoutObject(session, carts, ship, tax);
    }

}