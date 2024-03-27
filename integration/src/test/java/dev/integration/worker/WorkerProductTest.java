package dev.integration.worker;

import com.github.javafaker.Faker;
import dev.integration.MainTest;
import dev.integration.TestData;
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
class WorkerProductTest extends MainTest {

    @BeforeAll
    static void before() {
        assertNotNull(COOKIE);
    }

    @Test
    void shouldSuccessfullyRetrieveProducts() {
        testClient.get()
                .uri("/api/v1/worker/product")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldSuccessfullyCreateAProduct() throws IOException {
        var productDto = TestData
                .createProductDTO(
                        new Faker().commerce().productName(),
                        1,
                        TestData.sizeInventoryDTOArray(3)
                );

        // create the json
        String dto = mapper.writeValueAsString(productDto);

        MultiValueMap<String, Object> multipartData = TestData.files(dto);

        // request
        testClient.post()
                .uri("/api/v1/worker/product")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void shouldSuccessfullyUpdateAProduct() {
        var dto = TestData
                .updateProductDTO(
                        "product-uuid",
                        "new-product-name",
                        1
                );

        testClient.put()
                .uri("/api/v1/worker/product")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void shouldSuccessfullyDeleteAProduct() {
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
