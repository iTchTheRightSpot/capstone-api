package dev.webserver.payment;

import dev.webserver.AbstractUnitTest;
import dev.webserver.cart.ICartRepository;
import dev.webserver.cart.IShoppingSessionRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.shipping.ShipSetting;
import dev.webserver.shipping.ShippingService;
import dev.webserver.tax.Tax;
import dev.webserver.tax.TaxService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CheckoutServiceTest extends AbstractUnitTest {

    private CheckoutService checkoutService;

    @Mock
    private ShippingService shippingService;
    @Mock
    private TaxService taxService;
    @Mock
    private IShoppingSessionRepository sessionRepo;
    @Mock
    private ICartRepository ICartRepository;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(
                shippingService,
                taxService,
                sessionRepo,
                ICartRepository
        );
        checkoutService.setCartcookie("cartcookie");
        checkoutService.setSplit("%");
    }

    @Test
    void createCustomObjectForShoppingSession() {
        // given
        var session = ShoppingSession.builder().sessionId(1L).build();
        ShipSetting ship = new ShipSetting(1L, "nigeria", null, null);
        Tax tax = new Tax(1L, "vat", 0.075);
        Cookie[] cookies = {new Cookie("cartcookie", "this is custom cookie")};
        HttpServletRequest req = mock(HttpServletRequest.class);
        var cartItems = items.apply(3, session);

        // when
        when(req.getCookies()).thenReturn(cookies);
        when(sessionRepo.shoppingSessionByCookie(anyString())).thenReturn(Optional.of(session));
        when(ICartRepository.cartByShoppingSessionId(anyLong())).thenReturn(cartItems);
        when(shippingService.shippingByCountryElseReturnDefault(anyString())).thenReturn(ship);
        when(taxService.taxById(anyLong())).thenReturn(tax);

        // method to test
        CustomCheckoutObject obj = checkoutService
                .validateCurrentShoppingSession(req, "nigeria");

        // then
        Assertions.assertEquals(obj.session(), session);
        assertEquals(obj.cartItems(), cartItems);
        Assertions.assertEquals(obj.ship(), ship);
        Assertions.assertEquals(obj.tax(), tax);

        verify(sessionRepo, times(1)).shoppingSessionByCookie(anyString());
        verify(ICartRepository, times(1)).cartByShoppingSessionId(anyLong());
        verify(shippingService, times(1)).shippingByCountryElseReturnDefault(anyString());
        verify(taxService, times(1)).taxById(anyLong());
    }

    static final BiFunction<Integer, ShoppingSession, List<RaceConditionCartDbMapper>> items = (num, session) -> IntStream
            .range(0, num)
            .mapToObj(op -> new RaceConditionCartDbMapper((long) num, "sku-" + num, num, "size-" + num, (long) num, num * 2, session.sessionId()))
            .toList();

}