package dev.webserver.external.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
class LogConfiguration {

    private final LogEventPublisher publisher;

    @PostConstruct
    public void registerCustomAppender() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);

        final LogImpl obj = new LogImpl(publisher);
        obj.setContext(context);
        obj.start();

        log.addAppender(obj);
    }

}
