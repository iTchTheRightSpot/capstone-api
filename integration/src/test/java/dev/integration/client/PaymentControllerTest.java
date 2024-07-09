package dev.integration.client;

import dev.integration.AbstractNative;
import dev.integration.MockRequest;
import dev.webserver.cart.CartDto;
import dev.webserver.payment.PaymentResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentControllerTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cartcookie = MockRequest.CARTCOOKIE(testTemplate, route);

        assertNotNull(cartcookie);

        headers.set(HttpHeaders.COOKIE, cartcookie);
    }

    @Test
    void shouldSuccessfullyCallRaceConditionMethod() {
        // add to shopping cart
        var cart = testTemplate.postForEntity(
                route + "cart",
                new HttpEntity<>(new CartDto("product-sku-1", 4), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), cart.getStatusCode());

        // access race condition route
        var post = testTemplate.postForEntity(
                route + "payment?country=france&currency=ngn",
                new HttpEntity<>(headers),
                PaymentResponse.class
        );

        assertEquals(HttpStatusCode.valueOf(200), post.getStatusCode());
    }

}
