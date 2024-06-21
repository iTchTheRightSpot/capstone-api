package dev.integration.worker;

import dev.integration.AbstractNative;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest extends AbstractNative {
    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldSuccessfullyRetrieveAllUsers() {
        var get = testTemplate.exchange(
                route + "worker/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}
