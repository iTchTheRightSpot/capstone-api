package dev.webserver.external.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.AbstractEnvironment;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
class LogListener extends AbstractEnvironment implements ApplicationListener<LogEvent> {
    private static final Logger log = LoggerFactory.getLogger(LogListener.class);

    private final ObjectMapper mapper;
    private final RestClient restClient;

    LogListener(final ObjectMapper mapper, final RestClient.Builder client, final Environment environment) {
        super(environment);
        this.mapper = mapper;
        this.restClient = client.build();
    }

    @Async
    @SneakyThrows
    @Override
    public void onApplicationEvent(final LogEvent event) {
        if (activeprofile.endsWith("test")) return;

        while (!event.queue().isEmpty()) {
            final String payload = mapper.writeValueAsString(new DiscordPayload(event.queue().poll()));

            String body = restClient.post().uri(URI.create(discord))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class)
                    .getBody();

            log.info("LogListener publisher {}", body);
        }
    }
}
