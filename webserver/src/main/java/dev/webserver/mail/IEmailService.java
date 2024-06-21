package dev.webserver.mail;

public interface IEmailService {

    /**
     * Sends a confirmation email to the recipient that placed an order.
     *
     * @param to   the recipient's email address.
     * @param name the recipient's name.
     * @param detail the {@link ProductInformation} confirmed.
     */
    void confirmationEmail(final String to, final String name, final ProductInformation detail);

}
