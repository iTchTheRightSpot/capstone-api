package com.sarabrandserver.cart.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.product.entity.ProductSku;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CartControllerTest extends AbstractIntegrationTest {

    private final String path = "/api/v1/client/cart";

    ProductSku productSku() {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());
        return list.get(0);
    }

    @Test
    @WithMockUser(username = "client@client.com", password = "password", roles = {"CLIENT"})
    void create_new_shopping_session() throws Exception {
        var productSKU = productSku();

        var dto = new CartDTO(null, productSKU.getSku(), 10);

        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "client@client.com", password = "password", roles = {"CLIENT"})
    void add_to_existing_shopping_session() throws Exception {
        var productSKU = productSku();

    }

}