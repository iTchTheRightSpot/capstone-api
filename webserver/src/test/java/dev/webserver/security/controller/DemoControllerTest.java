package dev.webserver.security.controller;

import dev.webserver.AbstractIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class DemoControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}demo")
    private String path;

    @Test
    void demo() throws Exception {
        super.mockMvc.perform(get(path)).andExpect(status().isOk());
    }
}