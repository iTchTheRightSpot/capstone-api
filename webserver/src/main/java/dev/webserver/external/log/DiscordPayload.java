package dev.webserver.external.log;

import java.io.Serializable;

public record DiscordPayload(String content) implements Serializable {
    public DiscordPayload {
        if (content.length() > 2000) {
            content = content.substring(0, 2000);
        }
    }
}
