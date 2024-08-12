package dev.integration.employee;

import com.github.javafaker.Faker;
import dev.integration.AbstractNative;
import dev.integration.NativeTestData;
import dev.webserver.category.CategoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class NativeEmployeeProductTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();
    private static final String path = route + "employee/product";

    @Test
    void shouldSuccessfullyRetrieveProducts() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var get = testTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CategoryResponse.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyCreateAProduct() throws IOException {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);

        final var dto = NativeTestData
                .createProductDTO(
                        new Faker().commerce().productName(),
                        1,
                        NativeTestData.sizeInventoryDTOArray(3)
                );

        // create the json
        final var multipartData = NativeTestData.mockMultiPart(mapper.writeValueAsString(dto));

        // request
        var post = testTemplate.postForEntity(
                path,
                new HttpEntity<>(multipartData, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());
    }

    @Test
    void shouldSuccessfullyUpdateAProduct() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var dto = NativeTestData
                .updateProductDTO(
                        "product-uuid",
                        "new-product-name",
                        1
                );

        var update = testTemplate.exchange(
                path,
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
                path + "?id=product-uuid-2",
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
                path + "?id=product-uuid-1",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(409), delete.getStatusCode());
    }

}