package com.sarabrandserver.order.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}order")
    private String path;

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"CLIENT"})
    void orderHistory() throws Exception {
        this.MOCKMVC
                .perform(get(path))
                .andDo(print())
                .andExpect(status().isOk());
    }

}