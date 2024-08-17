package dev.integration.employee;

import dev.integration.AbstractNative;
import dev.webserver.category.CategoryDto;
import dev.webserver.category.CategoryResponse;
import dev.webserver.category.UpdateCategoryDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class NativeEmployeeCategoryTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();
    private final String path = route + "employee/category";

    @BeforeAll
    static void before() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldSuccessfullyRetrieveACategory() {
        var get = testTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyRetrieveProductsBaseOnCategory() {
        var get = testTemplate.exchange(
                path + "/products?category_id=1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyCreateACategory() {
        var post = testTemplate.postForEntity(
                path,
                new HttpEntity<>(new CategoryDto("worker-cat", true, null), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());
    }

    @Test
    void shouldSuccessfullyUpdateACategory() {
        var update = testTemplate.exchange(
                path,
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateCategoryDto(1L, null, "frank", false), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), update.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteACategory() {
        var delete = testTemplate.exchange(
                path + "/2",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

    @Test
    void shouldThrowErrorWhenDeletingACategoryAsItHasDetailsAttached() {
        var delete = testTemplate.exchange(
                path + "/1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(409), delete.getStatusCode());
    }

}
