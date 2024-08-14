package dev.webserver.exception;

import dev.webserver.AbstractIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class ControllerAdvicesTest extends AbstractIntegration {

    @Value("/${api.endpoint.baseurl}employee/test/with-param")
    private String url;

    @Test
    @WithMockUser(username = "emp-1@white.com", roles = {"EMPLOYEE"})
    void shouldTestRequestParameterMissingErrorResponse() throws Exception {
        super.mockMvc
                .perform(get(url).param("parameter", "hello world").with(csrf()))
                .andExpect(status().isOk());

        super.mockMvc
                .perform(get(url).param("parameter", "").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("parameter cannot be empty"));
    }

    @Test
    @WithMockUser(username = "emp-1@white.com", roles = {"EMPLOYEE"})
    void testMissingAnnotationInPostRequest() throws Exception {
        record Body(String parameter, Long[] numbers, Long number) {
        }

        super.mockMvc
                .perform(post(url)
                        .content(super.mapper.writeValueAsString(new Body("", new Long[]{1L}, 1L)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("parameter cannot be empty"));

        super.mockMvc
                .perform(post(url)
                        .content(super.mapper.writeValueAsString(new Body("ppp", new Long[]{1L}, null)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("number is required"));

        super.mockMvc
                .perform(post(url)
                        .content(super.mapper.writeValueAsString(new Body("parameters", new Long[]{1L}, 1L)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("parameter max length of 9"));

        super.mockMvc
                .perform(post(url)
                        .content(super.mapper.writeValueAsString(new Body(null, new Long[]{1L}, 1L)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("parameter cannot be empty"));

        super.mockMvc
                .perform(post(url)
                        .content(super.mapper.writeValueAsString(new Body("parameter", null, 1L)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("numbers cannot be empty"));

        super.mockMvc
                .perform(post(url)
                        .content(super.mapper.writeValueAsString(new Body("parameter", new Long[]{}, 1L)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("numbers cannot be empty"));
    }
}