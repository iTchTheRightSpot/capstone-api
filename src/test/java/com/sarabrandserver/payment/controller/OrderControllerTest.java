package com.sarabrandserver.payment.controller;

import com.sarabrandserver.AbstractIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class OrderControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}order")
    private String path;

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"CLIENT"})
    void orderHistory() throws Exception {
        this.mockMvc.perform(get(path)).andDo(print()).andExpect(status().isOk());
    }

}