package dev.integration.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import dev.integration.MainTest;
import dev.integration.TestData;
import dev.webserver.product.dto.SizeInventoryDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@AutoConfigureWebTestClient(timeout = "PT15M")
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
    void shouldSuccessfullyCreateAProduct() throws JsonProcessingException {
        SizeInventoryDTO[] dtos = {
                new SizeInventoryDTO(10, "small"),
                new SizeInventoryDTO(3, "medium"),
                new SizeInventoryDTO(15, "large"),
        };

        var dto = TestData
                .createProductDTO(
                        new Faker().commerce().productName(),
                        1,
                        dtos
                );

        // create the json
        String bytes = super.mapper.writeValueAsString(dto);

        System.out.println("toJson " + bytes);

        // as per https://github.com/spring-projects/spring-framework/issues/20666
        MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();

        // create the head
        HttpHeaders headers = new HttpHeaders();
        // add
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
        Resource imageResource = new ByteArrayResource("<<png data>>".getBytes()) {
            @Override
            public String getFilename() {
                return "image.png";
            }
        };

        multipartData.add("file", new HttpEntity<>(imageResource, headers));
        HttpHeaders metadataHeaders = new HttpHeaders();
        metadataHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        multipartData.add("dto", new HttpEntity<>(bytes, metadataHeaders));

        // request
        testClient.post()
                .uri("/api/v1/worker/product")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("files", multipartData))
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
