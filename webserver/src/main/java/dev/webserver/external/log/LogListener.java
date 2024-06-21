package dev.webserver.external.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
class LogListener implements ApplicationListener<LogEvent> {

    @Value("${application.log.webhook.discord}")
    private String discord;

    private final ObjectMapper mapper;
    private final RestClient restClient;

    LogListener(final ObjectMapper mapper, final RestClient.Builder client) {
        this.mapper = mapper;
        this.restClient = client.build();
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(LogEvent event) {
        while (event.queue().peek() != null) {
            final String payload = mapper.writeValueAsString(new DiscordPayload(event.queue().poll()));

            if (!discord.endsWith("test")) {
                restClient.post().uri(URI.create(discord))
                        .header("Content-Type", "application/json")
                        .body(payload)
                        .retrieve()
                        .body(Void.class);
            }
        }
    }

}
