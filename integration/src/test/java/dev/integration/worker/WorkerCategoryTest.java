package dev.integration.worker;

import dev.integration.MainTest;
import dev.webserver.category.dto.CategoryDTO;
import dev.webserver.category.dto.UpdateCategoryDTO;
import dev.webserver.category.response.WorkerCategoryResponse;
import dev.webserver.product.response.ProductResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class WorkerCategoryTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        assertNotNull(COOKIE);

        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Cookie", "JSESSIONID=" + COOKIE.getValue());
    }

    @Test
    void shouldSuccessfullyRetrieveACategory() {
        var get = testTemplate.exchange(
                PATH + "/api/v1/worker/category",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                WorkerCategoryResponse.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyRetrieveProductsBaseOnCategory() {
        var get = testTemplate.exchange(
                PATH + "/api/v1/worker/category/products?category_id=1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Page.class.asSubclass(ProductResponse.class)
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyCreateACategory() {
        var post = testTemplate.postForEntity(
                PATH + "/api/v1/worker/category",
                new HttpEntity<>(new CategoryDTO("worker-cat", true, null), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());
    }

    @Test
    void shouldSuccessfullyUpdateACategory() {
        var update = testTemplate.exchange(
                PATH + "/api/v1/worker/category",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateCategoryDTO(1L, null, "frank", false), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), update.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteACategory() {
        var delete = testTemplate.exchange(
                PATH + "/api/v1/worker/category/2",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

    @Test
    void shouldThrowErrorWhenDeletingACategoryAsItHasDetailsAttached() {
        var delete = testTemplate.exchange(
                PATH + "/api/v1/worker/category/1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(409), delete.getStatusCode());
    }

}
