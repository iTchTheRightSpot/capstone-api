package dev.integration.client;

import dev.integration.MainTest;
import dev.integration.MockRequest;
import dev.integration.TestData;
import dev.webserver.auth.dto.LoginDto;
import dev.webserver.auth.dto.RegisterDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    void setup() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Order(1)
    @Test
    void shouldSuccessfullyRegisterAUser() {
        var dto = new RegisterDto(
                "SEUY",
                "Development",
                "SEUY@SEUY.com",
                "",
                "0000000000",
                "password123"
        );

        var register = testTemplate.postForEntity(
                PATH + "api/v1/client/auth/register",
                new HttpEntity<>(dto, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), register.getStatusCode());
    }

    @Order(2)
    @Test
    void shouldSuccessfullyLoginAUser() {
        var login = testTemplate.postForEntity(
                PATH + "api/v1/client/auth/login",
                new HttpEntity<>(new LoginDto("SEUY@SEUY.com", "password123"), headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(200), login.getStatusCode());
    }

    @Order(3)
    @Test
    void shouldSuccessfullyRegisterUserToAnAdmin() {
        var cookie = MockRequest.ADMINCOOKIE(testTemplate, PATH);
        headers.set(HttpHeaders.COOKIE, cookie);

        var dto = new RegisterDto(
                "SEUY",
                "Development",
                "SEUY@SEUY.com",
                "",
                "0000000000",
                "password123"
        );

        var register = testTemplate.postForEntity(
                PATH + "api/v1/worker/auth/register",
                new HttpEntity<>(dto, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), register.getStatusCode());
    }

    @Order(4)
    @Test
    void shouldSuccessfullyRegisterAnAdmin() {
        var cookie = MockRequest.ADMINCOOKIE(testTemplate, PATH);
        headers.set(HttpHeaders.COOKIE, cookie);

        var dto = new RegisterDto(
                "SEUY",
                "Development",
                "fried@SEUY.com",
                "",
                "0000000000",
                "password123"
        );

        var register = testTemplate.postForEntity(
                PATH + "api/v1/worker/auth/register",
                new HttpEntity<>(dto, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), register.getStatusCode());
    }

}
