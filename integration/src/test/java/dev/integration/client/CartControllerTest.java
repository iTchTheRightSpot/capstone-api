package dev.integration.client;

import dev.integration.AbstractNative;
import dev.integration.MockRequest;
import dev.webserver.cart.dto.CartDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartControllerTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cartcookie = MockRequest.CARTCOOKIE(testTemplate, route);
        assertNotNull(cartcookie);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, cartcookie);
    }

    @Order(1)
    @Test
    void shouldSuccessfullyAddToAUsersCart() {
        // post
        var post = testTemplate.postForEntity(
                route + "cart",
                new HttpEntity<>(new CartDTO("product-sku-1", 5), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());
    }

    @Order(2)
    @Test
    void shouldSuccessfullyDeleteFromUsersCart() {
        // delete
        var delete = testTemplate.exchange(
                route + "cart?sku=product-sku-1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(200), delete.getStatusCode());
    }

}
