package dev.integration.worker;

import dev.integration.AbstractNative;
import dev.integration.MockRequest;
import dev.webserver.tax.TaxDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaxTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cookie = MockRequest.ADMINCOOKIE(testTemplate, PATH);
        assertNotNull(cookie);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, cookie);
    }

    @Order(1)
    @Test
    void shouldSuccessfullyRetrieveATaxDto() {
        var get = testTemplate.exchange(
                PATH + "api/v1/tax",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<TaxDto>>() {}
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Order(2)
    @Test
    void shouldSuccessfullyUpdateTax() {
        var put = testTemplate.exchange(
                PATH + "api/v1/tax",
                HttpMethod.PUT,
                new HttpEntity<>(new TaxDto(1L, "tax", 6.5), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), put.getStatusCode());
    }

}
