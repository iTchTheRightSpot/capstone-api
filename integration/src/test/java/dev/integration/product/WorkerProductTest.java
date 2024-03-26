package dev.integration.product;

import com.github.javafaker.Faker;
import dev.integration.MainTest;
import dev.integration.TestData;
import dev.webserver.product.dto.SizeInventoryDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
class WorkerProductTest extends MainTest {

    @BeforeAll
    static void before() {
        assertTrue(webserver.isRunning());
        ScriptUtils.runInitScript(
                new JdbcDatabaseDelegate(mysql, ""),
                "db/init.sql");
    }

    @Test
    void shouldSuccessfullyCreateAProduct() throws JsonProcessingException {
        var cookie = adminCookie();

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

        var payload = new MockMultipartFile(
                "dto",
                null,
                "application/json",
                mapper.writeValueAsString(dto).getBytes()
        );

        // request
        testClient.post()
                .uri("/api/v1/worker/product")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .cookie("JSESSIONID", cookie.getValue())
                .exchange()
                .expectStatus()
                .isCreated();
    }

}