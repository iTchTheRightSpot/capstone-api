package dev.webserver.external.mail;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class MailServiceImplTest {

    @Test
    void shouldVerifyApplicationLogoExists() {
        assertThat(MailServiceImpl.logo.exists()).isTrue();
    }

}