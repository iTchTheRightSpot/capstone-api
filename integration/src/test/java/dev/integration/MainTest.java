package dev.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.auth.dto.LoginDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainTest {

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final TestRestTemplate testTemplate = new TestRestTemplate();
    protected static String PATH;
    protected static String COOKIE;

    @Container
    @SuppressWarnings("all")
    private static DockerComposeContainer environment =
            new DockerComposeContainer(new File(Paths.get("../docker-compose.yaml").toUri()))
                    .withExposedService("mysql", 3306, Wait.forListeningPort())
                    .withExposedService("api", 1997, Wait.forListeningPort()
                                    .withStartupTimeout(Duration.ofMinutes(30)));

    @BeforeAll
    void beforeAllTests() throws SQLException {
        environment.start();

        final String host = environment.getServiceHost("api", 1997);
        final int port = environment.getServicePort("api", 1997);

        PATH = String.format("http://%s:%d/", host, port);
//        PATH = "http://localhost:1997/";

        CustomRunInitScripts.processScript("integration", "integration");

        COOKIE = adminCookie();
    }

    @Test
    void testCanImportAnObjectFromWebServerModule() {
        assertEquals(new LoginDto("principal@gmail.com", "password"),
                new LoginDto("principal@gmail.com", "password"));
    }

    @Test
    void actuator() throws JsonProcessingException {
        var responseEntity = testTemplate
                .getForEntity(PATH + "actuator/health", String.class);

        assertEquals(HttpStatusCode.valueOf(200), responseEntity.getStatusCode());

        var node = mapper.readValue(responseEntity.getBody(), JsonNode.class).get("status");

        assertEquals("UP", node.asText());
    }

    protected static String adminCookie() {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var post = testTemplate.postForEntity(
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

}
