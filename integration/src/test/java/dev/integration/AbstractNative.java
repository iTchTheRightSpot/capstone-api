package dev.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public abstract class AbstractNative {

    private static final Logger log = LoggerFactory.getLogger(AbstractNative.class);
    private static final Network network = Network.newNetwork();

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final TestRestTemplate testTemplate = new TestRestTemplate();

    protected static String PATH = "http://localhost:1997/";
    protected static String dburl = "jdbc:mysql://localhost:3306/capstone_db";
    protected static String dbUser = "capstone";
    protected static String dbPass = "capstone";

    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("capstone_db")
            .withUsername("capstone")
            .withPassword("capstone")
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .withLogConsumer(new Slf4jLogConsumer(log))
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(3)));

    private static final GenericContainer<?> container = new GenericContainer<>(
            DockerImageName.parse("capstone-api:latest"))
            .withNetwork(network)
            .withExposedPorts(1997)
            .withEnv("USER_PRINCIPAL", "admin@admin.com")
            .withEnv("USER_PASSWORD", "password123")
            .withEnv("SERVER_PORT", "1997")
            .withEnv("SPRING_PROFILES_ACTIVE", "native-test")
            .withEnv("API_PREFIX", "api/v1/")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:mysql://mysql:%d/capstone_db".formatted(mysql.getExposedPorts().getFirst()))
            .withEnv("SPRING_DATASOURCE_USERNAME", mysql.getUsername())
            .withEnv("SPRING_DATASOURCE_PASSWORD", mysql.getPassword())
            .withEnv("CORS_UI_DOMAIN", "http://localhost:4200/")
            .withEnv("SARRE_USD_TO_CENT", "100")
            .withEnv("SARRE_NGN_TO_KOBO", "0.37")
            .withEnv("AWS_PAYSTACK_SECRET_ID", "secrete-id")
            .dependsOn(mysql)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))
            .withLogConsumer(new Slf4jLogConsumer(log));

    static {
        final String pubKey = System.getenv("PAYSTACK_PUB_KEY");
        final String secretKey = System.getenv("PAYSTACK_SECRET_KEY");
        final String bucket = System.getenv("AWS_BUCKET");
        final String keyId = System.getenv("AWS_ACCESS_KEY_ID");
        final String accessKey = System.getenv("AWS_SECRET_ACCESS_KEY");

        if (pubKey != null && secretKey != null && bucket != null && keyId != null && accessKey != null) {
            container.withEnv("PAYSTACK_PUB_KEY", pubKey);
            container.withEnv("PAYSTACK_SECRET_KEY", secretKey);
            container.withEnv("AWS_BUCKET", bucket);
            container.withEnv("AWS_ACCESS_KEY_ID", keyId);
            container.withEnv("AWS_SECRET_ACCESS_KEY", accessKey);
        }

        if (Boolean.parseBoolean(System.getProperty("NATIVE_CI_PROFILE"))) {
            if (!mysql.isCreated() || mysql.isRunning()) {
                mysql.start();
                dburl = mysql.getJdbcUrl();
                dbUser = mysql.getUsername();
                dbPass = mysql.getPassword();
            }

            if (!container.isCreated() || !container.isRunning()) {
                container.start();
            }
            PATH = String.format("http://%s:%d/", container.getHost(), container.getFirstMappedPort());
        }

        try {
            CustomRunInitScripts.processScript(dburl, "capstone", "capstone");
        } catch (Exception e) {
            throw new RuntimeException("error running native test init sql script");
        }
    }

}
