package dev.webserver.external.mail;

import dev.webserver.AbstractEnvironment;
import dev.webserver.exception.CustomServerException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
class MailServiceImpl extends AbstractEnvironment implements IMailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    public static final File logo;

    static {
        try (final InputStream stream = MailServiceImpl.class.getClassLoader().getResourceAsStream("static/api/logo.jpeg")) {
            if (stream == null) throw new CustomServerException("application logo does not exist");
            logo = File.createTempFile("logo", ".jpeg");
            Files.copy(stream, logo.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("error retrieving logo {}", e.getMessage());
            throw new CustomServerException("failed to load application logo");
        }
    }

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    protected MailServiceImpl(final Environment environment, final JavaMailSender mailSender, final TemplateEngine templateEngine) {
        super(environment);
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @Override
    public void registrationEmail(final String to, final String firstname) {
        if (super.activeprofile.endsWith("test")) return;

        final Map<String, Object> map =
                Map.of("name", firstname, "owner", super.developerFirstname, "frontend", super.uiRedirect, "developer", super.developerEmail);

        send(to, "Confirmation of Account Registration", "mail-registration.html", map);
    }

    private void send(final String to, final String subject, final String emailTemplate, final Map<String, Object> variables) {
        try {
            final Context context = new Context();
            context.setVariables(variables);

            final MimeMessage message = mailSender.createMimeMessage();

            final MimeMessageHelper helper = new MimeMessageHelper(message, true, super.mailEncoding);
            helper.setPriority(1);
            helper.setSubject(subject);
            helper.setFrom(new InternetAddress(super.developerEmail));
            helper.setTo(new InternetAddress(to));
            helper.setText(templateEngine.process(emailTemplate, context), true);
            helper.addInline("logo", logo);

            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("email not sent {}", e.getMessage());
            throw new CustomServerException("An exception occurred. Please verify you email.");
        }
    }

}
