package dev.integration;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class MockRequest {

    public static String CARTCOOKIE(TestRestTemplate template, String route) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var get = template.exchange(
                route + "cart",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        var cookies = get.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        return cookies.stream()
                .filter(cookie -> cookie.startsWith("CARTCOOKIE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cart cookie is empty"));
    }

}
