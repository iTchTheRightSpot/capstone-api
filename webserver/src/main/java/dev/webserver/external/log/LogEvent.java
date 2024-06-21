package dev.webserver.external.log;

import org.springframework.context.ApplicationEvent;

import java.util.Queue;

final class LogEvent extends ApplicationEvent {

    private final Queue<String> queue;

    public LogEvent(Object source, Queue<String> queue) {
        super(source);
        this.queue = queue;
    }

    public Queue<String> queue() {
        return queue;
    }

}
