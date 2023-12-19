package com.sarabrandserver.order.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.order.dto.SkuQtyDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}payment")
    private String path;

    @Test
    @DisplayName("Test api call to purchase a product")
    void order() throws Exception {
        // given
        var arr = this.productSkuRepo
                .findAll()
                .stream()
                .limit(3)
                .map(obj -> new SkuQtyDTO(obj.getSku(), obj.getInventory() - 1))
                .toArray(SkuQtyDTO[]::new);

        var payment = TestingData.paymentDTO("frank@admin.com", SarreCurrency.USD, arr);

        // request
        this.MOCKMVC
                .perform(post(this.path + "/test")
                        .content(this.MAPPER.writeValueAsString(payment))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

}