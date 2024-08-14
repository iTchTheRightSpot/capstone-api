package dev.webserver.external.mail;

public interface IMailService {
    void registrationEmail(final String to, final String firstname);
}
