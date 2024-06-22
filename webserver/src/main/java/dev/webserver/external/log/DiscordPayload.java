package dev.webserver.external.log;

record DiscordPayload(String content) {
    public DiscordPayload {
        if (content.length() > 2000) {
            content = content.substring(0, 2000);
        }
    }
}
