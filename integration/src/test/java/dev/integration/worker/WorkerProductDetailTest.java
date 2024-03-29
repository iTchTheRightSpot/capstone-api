package dev.integration.worker;

import dev.integration.MainTest;
import dev.integration.TestData;
import dev.webserver.category.response.WorkerCategoryResponse;
import dev.webserver.product.dto.ProductDetailDto;
import dev.webserver.product.dto.UpdateProductDetailDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class WorkerProductDetailTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        assertNotNull(COOKIE);

        headers.set("Cookie", "JSESSIONID=" + COOKIE.getValue());
    }

    @Test
    void shouldSuccessfullyRetrieveProductDetails() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var get = testTemplate.exchange(
                PATH + "/api/v1/worker/product/detail?id=product-uuid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                WorkerCategoryResponse.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyCreateAProductDetail() throws IOException {
        headers.set("content-type", MediaType.MULTIPART_FORM_DATA_VALUE);

        var detailDto = new ProductDetailDto(
                "product-uuid",
                true,
                "brown",
                TestData.sizeInventoryDTOArray(3)
        );

        String dto = mapper.writeValueAsString(detailDto);

        MultiValueMap<String, Object> multipartData = TestData.files(dto);

        // request
        var post = testTemplate.postForEntity(
                PATH + "/api/v1/worker/product/detail",
                new HttpEntity<>(multipartData, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());
    }

    @Test
    void shouldSuccessfullyUpdateAProductDetail() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var dto = new UpdateProductDetailDto("product-sku-sku", "green", true, 4, "large");

        var update = testTemplate.exchange(
                PATH + "/api/v1/worker/product/detail",
                HttpMethod.PUT,
                new HttpEntity<>(dto, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), update.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteAProductDetail() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "/api/v1/worker/product/detail/product-sku-sku",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteAProductSku() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "/api/v1/worker/product/detail/sku?sku=product-sku-sku-2",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

}
