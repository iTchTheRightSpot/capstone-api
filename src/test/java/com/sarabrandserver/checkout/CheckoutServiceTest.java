package com.sarabrandserver.checkout;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.payment.projection.TotalPojo;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.shipping.entity.ShipSetting;
import com.sarabrandserver.shipping.service.ShippingService;
import com.sarabrandserver.tax.Tax;
import com.sarabrandserver.tax.TaxService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private ShoppingSessionRepo sessionRepo;
    @Mock
    private CartItemRepo cartItemRepo;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(
                shippingService,
                taxService,
                sessionRepo,
                cartItemRepo
        );
        checkoutService.setCART_COOKIE("cartcookie");
        checkoutService.setSPLIT("%");
    }

    List<TotalPojo> dummyItems (int num) {
        List<TotalPojo> list = new ArrayList<>();
        for (int i = 1; i < num; i++) {
            int finalI = i;
            TotalPojo item = new TotalPojo() {
                @Override
                public Integer getQty() {
                    return finalI;
                }

                @Override
                public BigDecimal getPrice() {
                    return new BigDecimal(new Faker().commerce().price());
                }

                @Override
                public Double getWeight() {
                    return new Faker().number()
                            .randomDouble(4, 1, 10);
                }
            };

            list.add(item);
        }
        return list;
    }

    @Test
    void createCustomObjectForShoppingSession() {
        // given
        ShoppingSession session = new ShoppingSession();
        session.setShoppingSessionId(1L);
        var items = List.of(
                new CartItem(1L, 2, session, new ProductSku()),
                new CartItem(2L, 4, session, new ProductSku()),
                new CartItem(3L, 1, session, new ProductSku())
        );
        ShipSetting ship = new ShipSetting();
        ship.setCountry("nigeria");
        Tax tax = new Tax(1L, "vat", 0.075);
        Cookie[] cookies = {new Cookie("cartcookie", "this is custom cookie")};
        HttpServletRequest req = mock(HttpServletRequest.class);

        // when
        when(req.getCookies()).thenReturn(cookies);
        when(sessionRepo.shoppingSessionByCookie(anyString()))
                .thenReturn(Optional.of(session));
        when(cartItemRepo.cartItemsByShoppingSessionId(anyLong()))
                .thenReturn(items);
        when(shippingService.shippingByCountryElseReturnDefault(anyString()))
                .thenReturn(ship);
        when(taxService.taxById(anyLong()))
                .thenReturn(tax);

        // method to test
        CustomObject obj = checkoutService
                .createCustomObjectForShoppingSession(req, "nigeria");

        // then
        assertEquals(obj.session(), session);
        assertEquals(obj.cartItems(), items);
        assertEquals(obj.ship(), ship);
        assertEquals(obj.tax(), tax);

        verify(sessionRepo, times(1))
                .shoppingSessionByCookie(anyString());
        verify(cartItemRepo, times(1))
                .cartItemsByShoppingSessionId(anyLong());
        verify(shippingService, times(1))
                .shippingByCountryElseReturnDefault(anyString());
        verify(taxService, times(1))
                .taxById(anyLong());
    }

}