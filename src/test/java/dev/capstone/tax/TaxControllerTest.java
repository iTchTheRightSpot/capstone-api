package dev.capstone.tax;

import dev.capstone.AbstractIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class TaxControllerTest extends AbstractIntegration {

    @Value("${api.endpoint.baseurl}tax")
    private String path;

    @Test
    @WithMockUser(username = "hello@hello.com", password = "password", roles = {"WORKER"})
    void taxes() throws Exception {
        this.mockMvc
                .perform(get( "/" + path).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("*").isArray())
                .andExpect(jsonPath("*").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "hello@hello.com", password = "password", roles = {"WORKER"})
    void update() throws Exception {
        // given
        var dto = new TaxDto(1L, "fish", 12.3256);

        // when
        this.mockMvc
                .perform(put( "/" + path)
                        .with(csrf())
                        .content(this.objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }

}