package dev.integration.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.integration.MainTest;
import dev.webserver.product.dto.ProductDetailDto;
import dev.webserver.product.dto.SizeInventoryDTO;
import dev.webserver.product.dto.UpdateProductDetailDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@AutoConfigureWebTestClient(timeout = "PT15M")
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
    void shouldSuccessfullyCreateAProductDetail() throws JsonProcessingException {
        SizeInventoryDTO[] dtos = {
                new SizeInventoryDTO(10, "small"),
                new SizeInventoryDTO(3, "medium"),
                new SizeInventoryDTO(15, "large"),
        };

        var dto = new ProductDetailDto("product-uuid", true, "brown", dtos);

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                super.mapper.writeValueAsString(dto).getBytes()
        );

        // request
        testClient.post()
                .uri("/api/v1/worker/product/detail")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromValue(payload))
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
                .uri("/api/v1/worker/product?id=product-uuid-2")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void shouldNotSuccessfullyDeleteAProduct() {
        testClient.delete()
                .uri("/api/v1/worker/product?id=product-uuid")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isEqualTo(409);
    }

}
