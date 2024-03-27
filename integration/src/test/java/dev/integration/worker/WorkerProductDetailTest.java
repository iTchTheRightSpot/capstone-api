package dev.integration.worker;

import dev.integration.MainTest;
import dev.integration.TestData;
import dev.webserver.product.dto.ProductDetailDto;
import dev.webserver.product.dto.UpdateProductDetailDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@AutoConfigureWebTestClient(timeout = "PT10H")
class WorkerProductDetailTest extends MainTest {

    @BeforeAll
    static void before() {
        assertNotNull(COOKIE);
    }

    @Test
    void shouldSuccessfullyRetrieveProductDetails() {
        testClient.get()
                .uri("/api/v1/worker/product/detail?id=product-uuid")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldSuccessfullyCreateAProductDetail() throws IOException {
        var detailDto = new ProductDetailDto(
                "product-uuid",
                true,
                "brown",
                TestData.sizeInventoryDTOArray(3)
        );

        String dto = mapper.writeValueAsString(detailDto);

        MultiValueMap<String, Object> multipartData = TestData.files(dto);

        // request
        testClient.post()
                .uri("/api/v1/worker/product/detail")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void shouldSuccessfullyUpdateAProductDetail() {
        var dto = new UpdateProductDetailDto("product-sku-sku", "green", true, 4, "large");

        testClient.put()
                .uri("/api/v1/worker/product/detail")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void shouldSuccessfullyDeleteAProductDetail() {
        testClient.delete()
                .uri("/api/v1/worker/product/detail/product-sku-sku")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void shouldSuccessfullyDeleteAProductSku() {
        testClient.delete()
                .uri("/api/v1/worker/product/detail/sku?sku=product-sku-sku-2")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
