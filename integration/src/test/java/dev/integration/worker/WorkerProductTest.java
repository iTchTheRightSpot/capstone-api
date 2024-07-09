package dev.integration.worker;

import com.github.javafaker.Faker;
import dev.integration.AbstractNative;
import dev.integration.TestData;
import dev.webserver.category.WorkerCategoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkerProductTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();

    @Test
    void shouldSuccessfullyRetrieveProducts() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var get = testTemplate.exchange(
                route + "worker/product",
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
                route + "worker/product",
                new HttpEntity<>(multipartData, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());

        // delete items saved in s3
//        var aws = testTemplate.getForEntity(PATH + "api/v1/native", String.class);
//        assertEquals(HttpStatusCode.valueOf(200), aws.getStatusCode());
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
                route + "worker/product",
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
                route + "worker/product?id=product-uuid-2",
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
                route + "worker/product?id=product-uuid-1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(409), delete.getStatusCode());
    }

}