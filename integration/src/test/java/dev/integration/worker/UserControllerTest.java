package dev.integration.worker;

import dev.integration.MainTest;
import dev.integration.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest extends MainTest {
    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        String cookie = TestData.ADMINCOOKIE(testTemplate, PATH);
        assertNotNull(cookie);

        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, cookie);
    }

    @Test
    void shouldSuccessfullyRetrieveAllUsers() {
        var get = testTemplate.exchange(
                PATH + "api/v1/worker/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}
