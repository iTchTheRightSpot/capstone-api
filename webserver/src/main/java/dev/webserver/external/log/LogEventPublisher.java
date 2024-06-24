package dev.webserver.external.log;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@RequiredArgsConstructor
class LogEventPublisher implements ILogEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishLog(final Queue<String> deque) {
        publisher.publishEvent(new LogEvent(this, deque));
    }

    @Override
    public void publishPurchase(final String name, final String email) {
        final LocalDateTime utc = LocalDateTime.now(ZoneOffset.UTC);
        final String date = utc.toLocalDate().format(DateTimeFormatter.ofPattern("E dd MMMM uuuu"));
        final String time = utc.toLocalTime().format(DateTimeFormatter.ofPattern("H:m a"));
        final String message = """
                ## __**Purchase Alert!**__ on %s at %s @everyone
                ### <--- Name: %s ---> <--- Email: %s --->
                """.formatted(date, time, name, email);

        publisher.publishEvent(new LogEvent(this, new ConcurrentLinkedDeque<>(List.of(message))));
    }

}
