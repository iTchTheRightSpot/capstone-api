package dev.integration;

import com.github.javafaker.Faker;
import dev.webserver.auth.dto.LoginDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    public static final String AdminEmail = "testadminemail@email.com";
    public static final String password = new Faker().lorem().characters(15);
    public static Map<String, String> map = new HashMap<>();
    private static WebTestClient testClient;

    @Container
    @ServiceConnection
//    @RestartScope
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("sara_brand_db")
            .withUsername("sara")
            .withPassword("sara");

    static {
        map.put("SPRING_PROFILES_ACTIVE", "native");
//        map.put("SERVER_PORT", "8080");
        map.put("USER_PRINCIPAL", AdminEmail);
        map.put("USER_PASSWORD", password);
        map.put("DB_DOMAIN", mysql.getDockerImageName());
        map.put("DB_HOST", mysql.getHost());
        map.put("DB_NAME", mysql.getDatabaseName());
        map.put("SPRING_DATASOURCE_USERNAME", mysql.getUsername());
        map.put("SPRING_DATASOURCE_PASSWORD", mysql.getPassword());
        map.put("CORS_UI_DOMAIN", "http://localhost:4200/");
        map.put("AWS_BUCKET", "http://localhost:4200/");
        map.put("AWS_PAYSTACK_SECRET_ID", "http://localhost:4200/");
        map.put("PAYSTACK_PUB_KEY", "public key");
        map.put("PAYSTACK_SECRET_KEY", "you tried it haha");
        map.put("SARRE_USD_TO_CENT", "100");
        map.put("SARRE_NGN_TO_KOB0", "0.37");
    }

    @Container
    private static final GenericContainer<?> webserver = new GenericContainer<>(
            new ImageFromDockerfile("native-image", false)
                    .withDockerfile(Paths.get("../Dockerfile")))
            .withExposedPorts(8080)
            .withEnv(map);

    @BeforeAll
    static void beforeAllTests() {
        mysql.start();
        webserver.start();
        String endpoint = String
                .format("http://%s:%d/", webserver.getHost(), webserver.getFirstMappedPort());

        testClient = WebTestClient.bindToServer().baseUrl(endpoint).build();
    }

    @Test
    void actuatorHealthNativeImage() {
        testClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("UP");
    }

    @Test
    void testCanImportAnObjectFromWebServerModule() {
        assertEquals(new LoginDto("principal@gmail.com", "password"),
                new LoginDto("principal@gmail.com", "password"));
    }

}