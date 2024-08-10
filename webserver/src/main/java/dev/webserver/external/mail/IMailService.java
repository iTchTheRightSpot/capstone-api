package dev.webserver.external.mail;

public interface IMailService {
    void registrationEmail(final String email, final String firstname);
}
