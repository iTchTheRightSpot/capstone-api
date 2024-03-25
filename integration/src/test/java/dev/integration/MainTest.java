package dev.integration;

import com.github.javafaker.Faker;
import dev.webserver.auth.dto.LoginDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    static Map<String, String> map = new HashMap<>();
    static final Network network = Network.newNetwork();
    private static WebTestClient testClient;

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("webserver_module_db")
            .withUsername("webserver_module")
            .withPassword("webserver_module")
//            .withInitScript("src/test/resources/db/init.sql")
            .withInitScript("db/init.sql")
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .withReuse(true);

    static {
        map.put("SPRING_PROFILES_ACTIVE", "native-test");
        map.put("SERVER_PORT", "8080");
        map.put("USER_PRINCIPAL", "testadminemail@email.com");
        map.put("USER_PASSWORD", new Faker().lorem().characters(15));
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
            .withExposedPorts(8080)
            .withReuse(true);

    @BeforeAll
    static void beforeAllTests() {
        mysql.start();

        assertTrue(mysql.isCreated());
        assertTrue(mysql.isRunning());

        webserver.start();

        assertTrue(webserver.isCreated());
        assertTrue(webserver.isRunning());

        String endpoint = String
                .format("http://%s:%d/", webserver.getHost(), webserver.getFirstMappedPort());

        testClient = WebTestClient.bindToServer().baseUrl(endpoint).build();
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

}