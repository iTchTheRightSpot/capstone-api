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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
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
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("webserver_module_db")
            .withUsername("webserver_module")
            .withPassword("webserver_module")
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .withReuse(true)
            .withLogConsumer(new Slf4jLogConsumer(log));

    static {
        map.put("SPRING_PROFILES_ACTIVE", "native-test");
        map.put("SERVER_PORT", "8081");
        map.put("API_PREFIX", "api/v1/");
        map.put("USER_PRINCIPAL", "admin@email.com");
        map.put("USER_PASSWORD", "password123");
        map.put("DB_DOMAIN", "mysql");
        map.put("DB_HOST", "3306");
        map.put("DB_NAME", "webserver_module_db");
        map.put("SPRING_DATASOURCE_USERNAME", "webserver_module");
        map.put("SPRING_DATASOURCE_PASSWORD", "webserver_module");
        map.put("CORS_UI_DOMAIN", "http://localhost:4200/");
        map.put("AWS_BUCKET", "webserver_module_bucket");
        map.put("AWS_PAYSTACK_SECRET_ID", "my-paystack-key");
        map.put("PAYSTACK_PUB_KEY", "public key");
        map.put("PAYSTACK_SECRET_KEY", "you tried it haha");
        map.put("SARRE_USD_TO_CENT", "100");
        map.put("SARRE_NGN_TO_KOB0", "0.37");
    }

    @Container
    private static final GenericContainer<?> webserver = new GenericContainer<>(
            new ImageFromDockerfile("webserver-module", false)
                    .withDockerfile(Paths.get("../Dockerfile")))
            .withNetwork(network)
            .dependsOn(mysql)
            .withEnv(map)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(MainTest.class)))
            .waitingFor(Wait.forHttp("/actuator/health"))
            .withExposedPorts(8081)
            .withStartupTimeout(Duration.of(60, ChronoUnit.MINUTES))
            .withReuse(true)
            .withLogConsumer(new Slf4jLogConsumer(log));

    @BeforeAll
    void beforeAllTests() {
        mysql.start();

        assertTrue(mysql.isCreated());
        assertTrue(mysql.isRunning());

        webserver.start();

        assertTrue(webserver.isCreated());
        assertTrue(webserver.isRunning());

        ScriptUtils.runInitScript(
                new JdbcDatabaseDelegate(mysql, ""),
                "db/init.sql");

        PATH = String
                .format("http://%s:%d/", webserver.getHost(), webserver.getFirstMappedPort());

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

        JsonNode node = mapper.readValue(responseEntity.getBody(), JsonNode.class).get("status");

        assertEquals("UP", node.asText());
    }

    protected static String adminCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var post = testTemplate.postForEntity(
                PATH + "api/v1/worker/auth/login",
                new HttpEntity<>(new LoginDto("admin@email.com", "password123"), headers),
                Void.class
        );

        var cookies = post.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        return cookies.stream()
                .filter(cookie -> cookie.startsWith("JSESSIONID"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("admin cookie is empty"));

//        return TestData.toCookie(jsessionid);
    }

}
