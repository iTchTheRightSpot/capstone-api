package dev.webserver.external.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

@RequiredArgsConstructor
class LogImpl extends AppenderBase<ILoggingEvent> {

    private static final Queue<String> queue = new ConcurrentLinkedDeque<>();
    private static final Set<String> set = Collections.synchronizedSet(new LinkedHashSet<>());

    private final LogEventPublisher publisher;

    @Override
    protected void append(ILoggingEvent event) {
        final IThrowableProxy proxy = event.getThrowableProxy();

        if (proxy != null) {
            final LocalDateTime utc = LocalDateTime.now(ZoneOffset.UTC);
            final String date = utc.toLocalDate().format(DateTimeFormatter.ofPattern("E dd MMMM uuuu"));
            final String time = utc.toLocalTime().format(DateTimeFormatter.ofPattern("H:m a"));
            final String url = requestUrl();

            final String message = """
                    ## __**%s**__ on %s at %s @everyone
                    ### Request Url %s
                    ### Exception -> %s
                    ```%s```
                    """.formatted(event.getLevel(), date, time, url, event.getFormattedMessage(), ThrowableProxyUtil.asString(proxy));

            if (!set.contains(message)) {
                queue.add(message);
                set.add(message);
                publisher.publishLog(queue);
            }
        }

        queue.clear();
        set.clear();
        queue.clear();
    }

    private String requestUrl() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            final HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return request.getRequestURL().toString();
        }
        return "";
    }

}
