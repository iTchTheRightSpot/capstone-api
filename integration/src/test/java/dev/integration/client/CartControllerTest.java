package dev.integration.client;

import dev.integration.MainTest;
import dev.integration.TestData;
import dev.webserver.cart.dto.CartDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartControllerTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cartcookie = TestData.CARTCOOKIE(testTemplate, PATH);
        assertNotNull(cartcookie);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, cartcookie);
    }

    @Order(1)
    @Test
    void shouldSuccessfullyAddToAUsersCart() {
        // post
        var post = testTemplate.postForEntity(
                PATH + "api/v1/cart",
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
                PATH + "api/v1/cart?sku=product-sku-1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(200), delete.getStatusCode());
    }

}
