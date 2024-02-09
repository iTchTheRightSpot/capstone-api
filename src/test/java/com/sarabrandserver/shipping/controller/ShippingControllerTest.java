package com.sarabrandserver.shipping.controller;

import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.shipping.ShippingDto;
import com.sarabrandserver.shipping.ShippingMapper;
import com.sarabrandserver.shipping.entity.Shipping;
import com.sarabrandserver.shipping.repository.ShippingRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ShippingControllerTest extends AbstractIntegration {

    @Value("${api.endpoint.baseurl}shipping")
    private String path;

    @Autowired
    private ShippingRepo shippingRepo;

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        this.MOCKMVC
                .perform(post("/" + path)
                        .with(csrf())
                        .content(this.MAPPER.writeValueAsString(
                                new ShippingDto(
                                        "Canada",
                                        new BigDecimal("10100"),
                                        new BigDecimal("20.24")
                                )
                        ))
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void update() throws Exception {
        var shipping = shippingRepo
                .save(new Shipping("Japan", new BigDecimal("25750"), new BigDecimal("35.55")));

        this.MOCKMVC
                .perform(put("/" + path)
                        .with(csrf())
                        .content(this.MAPPER.writeValueAsString(
                                new ShippingMapper(
                                        shipping.shippingId(),
                                        shipping.country(),
                                        new BigDecimal("10100"),
                                        new BigDecimal("20.24")
                                )
                        ))
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteShipping() throws Exception {
        var shipping = shippingRepo
                .save(new Shipping("France", new BigDecimal("25750"), new BigDecimal("35.55")));

        this.MOCKMVC
                .perform(delete("/" + path + "/" + shipping.shippingId())
                        .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

}