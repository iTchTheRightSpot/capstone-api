package dev.integration.worker;

import dev.integration.AbstractNative;
import dev.integration.MockRequest;
import dev.webserver.shipping.ShippingDto;
import dev.webserver.shipping.ShippingMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShippingTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();
    private final String path = PATH + "api/v1/shipping";

    @BeforeAll
    static void before() {
        String cookie = MockRequest.ADMINCOOKIE(testTemplate, PATH);
        assertNotNull(cookie);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, cookie);
    }

    @Test
    @Order(1)
    void shouldSuccessfullyRetrieveShippingObjects() {
        var get = testTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ShippingMapper>>() {}
        );
        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    @Order(2)
    void shouldSuccessfullyCreateShippingDetails() {
        var post = testTemplate.postForEntity(
                path,
                new HttpEntity<>(new ShippingDto("brazil",
                        new BigDecimal("2410"), new BigDecimal("15.99")),
                        headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());
    }

    @Test
    @Order(3)
    void shouldSuccessfullyUpdateShippingSetting() {
        var put = testTemplate.exchange(
                path,
                HttpMethod.PUT,
                new HttpEntity<>(new ShippingMapper(3L, "Ecuador",
                        new BigDecimal("2410"), new BigDecimal("15.99")),
                        headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), put.getStatusCode());
    }

    @Test
    @Order(4)
    void shouldSuccessfullyDeleteShippingSetting() {
        var delete = testTemplate.exchange(
                path + "/3",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

    @Test
    @Order(5)
    void shouldNotSuccessfullyDeleteDefaultShippingSetting() {
        var delete = testTemplate.exchange(
                path + "/1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(409), delete.getStatusCode());
    }

}
