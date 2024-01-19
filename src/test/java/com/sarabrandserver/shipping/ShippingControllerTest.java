package com.sarabrandserver.shipping;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.enumeration.ShippingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShippingControllerTest extends AbstractIntegrationTest {

    @Value("${api.endpoint.baseurl}shipping")
    private String path;

    @Autowired
    private ShippingRepo shippingRepo;

    @BeforeEach
    void before() {
        shippingRepo.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        this.MOCKMVC
                .perform(post("/" + path)
                        .with(csrf())
                        .content(this.MAPPER.writeValueAsString(
                                new ShippingDto(new BigDecimal("25750"),
                                        new BigDecimal("30.54"), ShippingType.INTERNATIONAL.name())
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());

        this.MOCKMVC
                .perform(post("/" + path)
                        .with(csrf())
                        .content(this.MAPPER.writeValueAsString(
                                new ShippingDto(new BigDecimal("10100"),
                                        new BigDecimal("20.24"), ShippingType.LOCAL.name())
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void update() throws Exception {
        var shipping = shippingRepo
                .save(new Shipping(new BigDecimal("25750"), new BigDecimal("35.55"), ShippingType.LOCAL));

        this.MOCKMVC
                .perform(put("/" + path)
                        .with(csrf())
                        .content(this.MAPPER.writeValueAsString(
                                new ShippingUpdateDto(
                                        shipping.shippingId(),
                                        new BigDecimal("10100"),
                                        new BigDecimal("20.24")
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        var change = shippingRepo.findById(shipping.shippingId());
        assertFalse(change.isEmpty());
        assertEquals(new BigDecimal("10100.00"), change.get().ngnPrice());
        assertEquals(new BigDecimal("20.24"), change.get().usdPrice());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteShipping() throws Exception {
        var shipping = shippingRepo
                .save(new Shipping(new BigDecimal("25750"), new BigDecimal("35.55"), ShippingType.LOCAL));

        this.MOCKMVC
                .perform(delete("/" + path + "/" + shipping.shippingId())
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        assertTrue(shippingRepo.findById(shipping.shippingId()).isEmpty());
    }

}