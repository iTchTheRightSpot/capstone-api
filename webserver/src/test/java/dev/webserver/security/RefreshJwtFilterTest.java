package dev.webserver.security;

import dev.webserver.AbstractIntegration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class RefreshJwtFilterTest extends AbstractIntegration {

    @Test
    void jwtShouldBeRefreshedIfWithinExpiration() throws Exception {
//        final String jwt = generateShortLivedJwt();

    }

}