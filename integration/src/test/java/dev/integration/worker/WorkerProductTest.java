package dev.integration.worker;

import com.github.javafaker.Faker;
import dev.integration.MainTest;
import dev.integration.MockRequest;
import dev.integration.TestData;
import dev.webserver.category.response.WorkerCategoryResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkerProductTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cookie = MockRequest.ADMINCOOKIE(testTemplate, PATH);
        assertNotNull(cookie);

        headers.set(HttpHeaders.COOKIE, cookie);
    }

    @Test
    void shouldSuccessfullyRetrieveProducts() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var get = testTemplate.exchange(
                PATH + "api/v1/worker/product",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                WorkerCategoryResponse.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyCreateAProduct() throws IOException {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);

        final var dto = TestData
                .createProductDTO(
                        new Faker().commerce().productName(),
                        1,
                        TestData.sizeInventoryDTOArray(3)
                );

        // create the json
        final var multipartData = TestData.mockMultiPart(mapper.writeValueAsString(dto));

        // request
        var post = testTemplate.postForEntity(
                PATH + "api/v1/worker/product",
                new HttpEntity<>(multipartData, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());

        // delete items saved in s3
        var aws = testTemplate.postForEntity(
                PATH + "api/v1/native",
                new HttpEntity<>(null, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), aws.getStatusCode());
    }

    @Test
    void shouldSuccessfullyUpdateAProduct() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var dto = TestData
                .updateProductDTO(
                        "product-uuid",
                        "new-product-name",
                        1
                );

        var update = testTemplate.exchange(
                PATH + "api/v1/worker/product",
                HttpMethod.PUT,
                new HttpEntity<>(dto, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), update.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteAProduct() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "api/v1/worker/product?id=product-uuid-2",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

    @Test
    void shouldNotSuccessfullyDeleteAProduct() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "api/v1/worker/product?id=product-uuid-1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(409), delete.getStatusCode());
    }

}
