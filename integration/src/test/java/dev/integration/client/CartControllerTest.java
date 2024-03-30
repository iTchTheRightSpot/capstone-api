package dev.integration.client;

import dev.integration.MainTest;
import dev.webserver.cart.dto.CartDTO;
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

    private static String CARTCOOKIE;

    @BeforeAll
    static void before() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var get = testTemplate.exchange(
                PATH + "api/v1/cart",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ArrayList.class
        );

        var cookies = get.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        CARTCOOKIE = cookies.stream()
                .filter(cookie -> cookie.startsWith("CARTCOOKIE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cart cookie is empty"));

//        CARTCOOKIE = TestData.toCookie(str);

        assertNotNull(CARTCOOKIE);
    }

    @Test
    void shouldSuccessfullyAddToAUsersCart_should_successfully_delete_from_users_cart() {
        // header
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String string = CARTCOOKIE;
        headers.set(HttpHeaders.SET_COOKIE, string);

        // post
        var post = testTemplate.postForEntity(
                PATH + "api/v1/cart",
                new HttpEntity<>(new CartDTO("product-sku-sku", 5), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());

        // delete
        ResponseEntity<Void> delete = testTemplate.exchange(
                PATH + "api/v1/cart?sku=product-sku-sku",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(200), delete.getStatusCode());
    }

}
