package dev.integration.worker;

import dev.integration.AbstractNative;
import dev.integration.CustomRunInitScripts;
import dev.integration.MockRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CronControllerTest extends AbstractNative {

    @Test
    void shouldSuccessfullyTestCronJobMethodNativeMode() throws SQLException {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        CustomRunInitScripts.insertDummyOrderReservation(dburl, dbUser, dbPass);

        final var get = testTemplate.exchange(
                route + "cron",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Void.class
        );
        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}
