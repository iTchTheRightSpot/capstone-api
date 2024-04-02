package dev.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.auth.dto.LoginDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class MainTest {

    private static final Logger log = LoggerFactory.getLogger(MainTest.class);
    private static final Map<String, String> map = new HashMap<>();
    private static final Network network = Network.newNetwork();

    protected static ObjectMapper mapper = new ObjectMapper();
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
    void beforeAllTests() {
        environment.start();

        var mysqlOptional = environment.getContainerByServiceName("mysql");
        var apiOptional = environment.getContainerByServiceName("api");


        assertTrue(mysqlOptional.isPresent());
        assertTrue(apiOptional.isPresent());

        ScriptUtils.runInitScript(
                new JdbcDatabaseDelegate((JdbcDatabaseContainer<?>) mysqlOptional.get(), ""),
                "db/init.sql");

        final var mySqlHostname = environment.getServiceHost("api", 1997);
        final var hostMySqlProtocolPort = environment.getServicePort("api", 1997);

        PATH = String
                .format("http://%s:%d/", mySqlHostname, hostMySqlProtocolPort);

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
