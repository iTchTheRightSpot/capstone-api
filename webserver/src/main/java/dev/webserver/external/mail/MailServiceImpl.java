package dev.webserver.external.mail;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
class MailServiceImpl implements IMailService {

    @Async
    @Override
    public void registrationEmail(final String email, final String firstname) {

    }
}
