package dev.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainTest {

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final TestRestTemplate testTemplate = new TestRestTemplate();
    protected static String PATH;

    // https://java.testcontainers.org/modules/docker_compose/
    @Container
    @SuppressWarnings("all")
    private static DockerComposeContainer environment =
            new DockerComposeContainer(new File(Paths.get("../docker-compose.yaml").toUri()))
                    .withExposedService("mysql", 3306, Wait.forListeningPort())
//                    .withExposedService("api", 1997, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(30)))
                    .withExposedService("api", 1997, Wait.forHttp("/actuator/health").forStatusCode(200))
                    .withLogConsumer("api", new Slf4jLogConsumer(LoggerFactory.getLogger(MainTest.class)));

    @BeforeAll
    void beforeAllTests() throws SQLException {
        environment.start();

        final String host = environment.getServiceHost("api", 1997);
        final int port = environment.getServicePort("api", 1997);

        PATH = String.format("http://%s:%d/", host, port);
//        PATH = "http://localhost:1997/";

        CustomRunInitScripts.processScript("integration", "integration");
    }

}
