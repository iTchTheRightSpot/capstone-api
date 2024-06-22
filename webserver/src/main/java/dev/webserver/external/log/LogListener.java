package dev.webserver.external.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
class LogListener implements ApplicationListener<LogEvent> {
    private static final Logger log = LoggerFactory.getLogger(LogListener.class);

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final BiFunction<URI, String, HttpRequest> request = (uri, payload) -> HttpRequest
            .newBuilder(uri)
            .timeout(Duration.ofSeconds(10))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

    @Value("${application.log.webhook.discord}")
    private String discord;
    @Value("${spring.profiles.active}")
    private String profile;

    private final ObjectMapper mapper;

    @SneakyThrows
    @Override
    public void onApplicationEvent(final LogEvent event) {
        if (profile.endsWith("test")) return;

        while (!event.queue().isEmpty()) {
            final String payload = mapper.writeValueAsString(new DiscordPayload(event.queue().poll()));

            log.info("LogListener payload {}", payload);

            client.sendAsync(request.apply(URI.create(discord), payload), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(e -> log.info("LogListener response {}", e))
                    .join();
        }
    }
}
