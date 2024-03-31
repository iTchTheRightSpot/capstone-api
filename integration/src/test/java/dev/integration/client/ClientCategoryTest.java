package dev.integration.client;

import dev.integration.MainTest;
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
class ClientCategoryTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    void before() {
        assertNotNull(COOKIE);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, COOKIE);
    }

    @Test
    void shouldSuccessfullyRetrieveAllCategories() {
        var get = testTemplate.exchange(
                PATH + "api/v1/client/category",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ArrayList.class
                // new ParameterizedTypeReference<List<CategoryResponse>>() {}
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyRetrieveProductsBaseOnCategory() {
        var get = testTemplate.exchange(
                PATH + "api/v1/client/category/products?category_id=1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}
