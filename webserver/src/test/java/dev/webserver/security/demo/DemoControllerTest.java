package dev.webserver.security.demo;

import dev.webserver.AbstractIntegration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class DemoControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}")
    private String apiprefix;
    @Value("${server.servlet.session.cookie.name}")
    private String jsessionid;

    @Test
    void demo() throws Exception {
        final String dto = super.mapper
                .writeValueAsString(new LoginDto("demo@capstone.com", "password123"));

        final String jwt = Objects
                .requireNonNull(super.mockMvc
                        .perform(post(apiprefix + "demo")
                                .content(dto)
                                .contentType("application/json")
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getCookie(jsessionid))
                .getValue();

        super.mockMvc
                .perform(get(apiprefix + "order/test")
                .cookie(new Cookie(jsessionid, jwt)))
                .andExpect(status().isOk());

        super.mockMvc
                .perform(get(apiprefix + "employee/test")
                .cookie(new Cookie(jsessionid, jwt)))
                .andExpect(status().isOk());

        super.mockMvc
                .perform(post(apiprefix + "employee/test")
                        .cookie(new Cookie(jsessionid, jwt)))
                .andExpect(status().isForbidden());
    }
}