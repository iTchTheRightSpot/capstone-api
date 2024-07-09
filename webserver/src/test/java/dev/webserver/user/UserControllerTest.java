package dev.webserver.user;

import dev.webserver.AbstractIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}worker")
    private String path;

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allUsers() throws Exception {
        this.mockMvc
                .perform(get(path + "/user").with(csrf()))
                .andExpect(status().isOk());
    }

}