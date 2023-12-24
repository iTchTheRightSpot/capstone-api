package com.sarabrandserver.user.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}worker")
    private String path;

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allUsers() throws Exception {
        this.MOCKMVC
                .perform(get(path + "/user").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

}