package dev.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.auth.dto.LoginDto;
import dev.webserver.cart.response.CartResponse;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MockRequest {

    public static String CARTCOOKIE(TestRestTemplate restTemplate, String PATH) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var get = restTemplate.exchange(
                PATH + "api/v1/cart",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<CartResponse>>() {}
        );

        var cookies = get.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        return cookies.stream()
                .filter(cookie -> cookie.startsWith("CARTCOOKIE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cart cookie is empty"));
    }

    public static String ADMINCOOKIE(TestRestTemplate restTemplate, String PATH) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var post = restTemplate.postForEntity(
                PATH + "api/v1/worker/auth/login",
                new HttpEntity<>(new LoginDto("admin@admin.com", "password123"), headers),
                Void.class
        );

        var cookies = post.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        return cookies.stream()
                .filter(cookie -> cookie.startsWith("JSESSIONID"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("admin cookie is empty"));
    }

    /**
     * Returns a {@link CsrfToken} token.
     * */
    public static BiFunction<TestRestTemplate, String, String> CSRF = (template, path) -> {
        String body = template.getForEntity(path + "api/v1/csrf", String.class).getBody();

        assertNotNull(body);
        try {
            return new ObjectMapper().readValue(body, JsonNode.class).get("token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    };

}
