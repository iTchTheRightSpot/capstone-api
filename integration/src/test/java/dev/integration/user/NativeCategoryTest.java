package dev.integration.user;

import dev.integration.AbstractNative;
import dev.webserver.category.CategoryResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class NativeCategoryTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    void before() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldSuccessfullyRetrieveAllCategories() {
        final var get = testTemplate.exchange(
                route + "category",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<CategoryResponse>>() {}
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyRetrieveProductsBaseOnCategory() {
        final var get = testTemplate.exchange(
                route + "category/products?category_id=1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}