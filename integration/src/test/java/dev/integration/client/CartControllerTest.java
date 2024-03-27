package dev.integration.client;

import dev.integration.MainTest;
import dev.webserver.cart.dto.CartDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@AutoConfigureWebTestClient(timeout = "PT10H")
class CartControllerTest extends MainTest {

    private static ResponseCookie CARTCOOKIE;

    @BeforeAll
    static void before() {
        var fluxExchangeResult = testClient.get()
                .uri("/api/v1/cart")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .exists(HttpHeaders.SET_COOKIE)
                .returnResult(Void.class);

        var cookies = fluxExchangeResult.getResponseCookies().get("CARTCOOKIE");

        if (cookies.isEmpty())
            throw new RuntimeException("cart cookie is empty");

        CARTCOOKIE = cookies.getFirst();

        assertNotNull(CARTCOOKIE);
    }

    @Test
    void shouldSuccessfullyAddToAUsersCart_should_successfully_delete_from_users_cart() {
        var dto = new CartDTO("product-sku-sku", 5);
        testClient.post()
                .uri("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .cookie("CARTCOOKIE", CARTCOOKIE.getValue())
                .exchange()
                .expectStatus()
                .isCreated();


        testClient.delete()
                .uri("/api/v1/cart?sku=product-sku-sku")
                .cookie("CARTCOOKIE", CARTCOOKIE.getValue())
                .exchange()
                .expectStatus()
                .isOk();
    }

}
