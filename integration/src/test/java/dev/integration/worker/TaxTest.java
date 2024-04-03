package dev.integration.worker;

import dev.integration.MainTest;
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
class TaxTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        assertNotNull(COOKIE);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, COOKIE);
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
        var get = testTemplate.exchange(
                PATH + "api/v1/tax",
                HttpMethod.PUT,
                new HttpEntity<>(new TaxDto(1L, "VAT-TAX", 6.5), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}
