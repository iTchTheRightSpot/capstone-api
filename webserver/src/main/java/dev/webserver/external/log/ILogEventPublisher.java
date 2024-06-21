package dev.webserver.external.log;

import java.util.Queue;

/**
 * Interface for publishing log events and user actions to a third-party service.
 */
public interface ILogEventPublisher {

    /**
     * Publishes application logs to the configured third-party service.
     * This method is intended to handle and transmit a queue of log messages.
     *
     * @param deque A queue containing log messages to be published.
     */
    void publishLog(final Queue<String> deque);

    /**
     * Publishes an event indicating a user sign-in or registration to the configured third-party service.
     * This method is intended to notify the application developer about user activities. Specifically
     * the engagement of our application.
     *
     * @param name  The name of the user who signed in or registered.
     * @param email The email address of the user who signed in or registered.
     */
    void publishPurchase(final String name, final String email);

}
