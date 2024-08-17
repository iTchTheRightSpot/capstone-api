package dev.integration.user;

import dev.integration.AbstractNative;
import dev.webserver.security.demo.DemoUser;
import dev.webserver.security.demo.LoginDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class NativeDemoControllerTest extends AbstractNative {

    @Test
    void shouldSuccessfullyLoginDemoAccount() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        final var login = testTemplate.postForEntity(
                route + "demo",
                new HttpEntity<>(new LoginDto(DemoUser.demo.email(), DemoUser.password), headers),
                Void.class
        );

        assertThat(HttpStatusCode.valueOf(200)).isEqualTo(login.getStatusCode());
    }
}
