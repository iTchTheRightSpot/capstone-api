package dev.webserver.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class PageTest {

    @Test
    void shouldAccuratelyCalculatePageOffset() {
        assertThat(Page.of(0, 20).offset()).isEqualTo(0);
        assertThat(Page.of(1, 20).offset()).isEqualTo(20);
        assertThat(Page.of(2, 20).offset()).isEqualTo(40);
    }

}