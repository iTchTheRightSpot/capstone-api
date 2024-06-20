package dev.integration.worker;

import dev.integration.AbstractNative;
import dev.integration.MockRequest;
import dev.integration.TestData;
import dev.webserver.product.dto.ProductDetailDto;
import dev.webserver.product.dto.UpdateProductDetailDto;
import dev.webserver.product.response.DetailResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.COOKIE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkerProductDetailTest extends AbstractNative {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cookie = MockRequest.ADMINCOOKIE(testTemplate, PATH);
        assertNotNull(cookie);

        headers.set(COOKIE, cookie);
    }

    @Test
    void shouldSuccessfullyRetrieveProductDetails() {
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        var get = testTemplate.exchange(
                PATH + "api/v1/worker/product/detail?id=product-uuid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<DetailResponse>>() {}
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyCreateAProductDetail() throws IOException {
        headers.set(CONTENT_TYPE, MULTIPART_FORM_DATA_VALUE);

        var dto = new ProductDetailDto(
                "product-uuid-1",
                true,
                "brown",
                TestData.sizeInventoryDTOArray(3)
        );

        MultiValueMap<String, Object> multipartData = TestData.mockMultiPart(mapper.writeValueAsString(dto));

        // request
        var post = testTemplate.postForEntity(
                PATH + "api/v1/worker/product/detail",
                new HttpEntity<>(multipartData, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());

        // delete items saved in s3
//        var aws = testTemplate.getForEntity(PATH + "api/v1/native", String.class);
//        assertEquals(HttpStatusCode.valueOf(200), aws.getStatusCode());
    }

    @Test
    void shouldSuccessfullyUpdateAProductDetail() {
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        var dto = new UpdateProductDetailDto("product-sku-2", "green", true, 4, "large");

        var update = testTemplate.exchange(
                PATH + "api/v1/worker/product/detail",
                HttpMethod.PUT,
                new HttpEntity<>(dto, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), update.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteAProductDetail() {
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "api/v1/worker/product/detail/product-sku-3",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteAProductSku() {
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "api/v1/worker/product/detail/sku?sku=product-sku-2",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

}