package com.sarabrandserver.order.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.product.entity.ProductSku;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}payment")
    private String path;
    @Value(value = "/${api.endpoint.baseurl}cart")
    private String cartPath;
    @Value("${cart.cookie.name}")
    private String CART_COOKIE;

    private ProductSku productSku() {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());
        return list.get(0);
    }

    private Cookie create_new_shopping_session() throws Exception {
        MvcResult result = this.MOCKMVC
                .perform(get(cartPath).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = productSku();

        this.MOCKMVC
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER
                                .writeValueAsString(new CartDTO(sku.getSku(), sku.getInventory()))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        return cookie;
    }

    @Test
    @DisplayName("Tests against race condition")
    void validate() throws Exception {
        // simulate adding item to cart
        Cookie cookie = create_new_shopping_session();

        // request
        this.MOCKMVC
                .perform(get(this.path)
                        .param("currency", USD.getCurrency())
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());
    }

}