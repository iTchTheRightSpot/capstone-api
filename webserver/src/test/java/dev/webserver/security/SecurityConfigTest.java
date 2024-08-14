package dev.webserver.security;

import dev.webserver.AbstractIntegration;
import dev.webserver.exception.ExceptionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class SecurityConfigTest extends AbstractIntegration {

    @Value("/${api.endpoint.baseurl}employee/test")
    private String url;

    @Test
    void redirectPathFor401RequestShouldNotBeEmpty() throws Exception {
        final String response = super.mockMvc
                .perform(get(url).with(csrf()))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(super.mapper.readValue(response, ExceptionResponse.class).redirect_url())
                .contains("authentication/authenticate");
    }
}