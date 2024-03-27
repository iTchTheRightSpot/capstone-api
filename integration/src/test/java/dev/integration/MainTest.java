package dev.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.auth.dto.LoginDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
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
    protected static WebTestClient testClient;
    protected static ResponseCookie COOKIE;

    @Container
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
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
    protected static final GenericContainer<?> webserver = new GenericContainer<>(
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

        String endpoint = String
                .format("http://%s:%d/", webserver.getHost(), webserver.getFirstMappedPort());

        testClient = WebTestClient.bindToServer().baseUrl(endpoint)
                .build();

        COOKIE = adminCookie();
    }

    @Test
    void testCanImportAnObjectFromWebServerModule() {
        assertEquals(new LoginDto("principal@gmail.com", "password"),
                new LoginDto("principal@gmail.com", "password"));
    }

    @Test
    void actuator() {
        testClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("UP");
    }

    protected static ResponseCookie adminCookie() {
        FluxExchangeResult<Void> result = testClient.post()
                .uri("/api/v1/worker/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters
                        .fromValue(new LoginDto("admin@email.com", "password123")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .exists(HttpHeaders.SET_COOKIE)
                .returnResult(Void.class);

        // print results
        Flux<Void> body = result.getResponseBody();

        body.subscribe(b -> System.out.println("Response Body: " + b));

        MultiValueMap<String, ResponseCookie> cookies = result.getResponseCookies();

        List<ResponseCookie> jsessionid = cookies.get("JSESSIONID");

        if (jsessionid.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        return jsessionid.getFirst();
    }

}
