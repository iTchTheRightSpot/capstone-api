package dev.integration.client;

import dev.integration.MainTest;
import dev.integration.TestData;
import dev.webserver.cart.dto.CartDTO;
import dev.webserver.cart.response.CartResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class CartControllerTest extends MainTest {

    private static ResponseCookie CARTCOOKIE;

    @BeforeAll
    static void before() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var response = testTemplate.exchange(
                PATH + "/api/v1/cart",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ArrayList.class.asSubclass(CartResponse.class)
        );

        var cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        String str = cookies.stream()
                .filter(cookie -> cookie.startsWith("CARTCOOKIE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cart cookie is empty"));

        CARTCOOKIE = TestData.toCookie(str);

        assertNotNull(CARTCOOKIE);
    }

    @Test
    void shouldSuccessfullyAddToAUsersCart_should_successfully_delete_from_users_cart() {
        // header
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.SET_COOKIE, CARTCOOKIE.getValue());

        // post
        var post = testTemplate.postForEntity(
                PATH + "/api/v1/cart",
                new HttpEntity<>(new CartDTO("product-sku-sku", 5), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());

        // delete
        ResponseEntity<Void> delete = testTemplate.exchange(
                PATH + "/api/v1/cart?sku=product-sku-sku",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(200), delete.getStatusCode());
    }

}
