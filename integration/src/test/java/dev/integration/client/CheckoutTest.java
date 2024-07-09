package dev.integration.client;

import dev.integration.AbstractNative;
import dev.integration.MockRequest;
import dev.webserver.cart.CartDTO;
import dev.webserver.checkout.Checkout;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CheckoutTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cartcookie = MockRequest.CARTCOOKIE(testTemplate, route);

        assertNotNull(cartcookie);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, cartcookie);
    }

    @Test
    void shouldSuccessfullyAccessCheckoutRoute() {
        // add to shopping cart
        var post = testTemplate.postForEntity(
                route + "cart",
                new HttpEntity<>(new CartDTO("product-sku-1", 3), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());

        // access checkout route
        var get = testTemplate.exchange(
                route + "checkout?country=nigeria&currency=ngn",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Checkout.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}
