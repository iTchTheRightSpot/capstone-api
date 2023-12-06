package com.sarabrandserver.cart.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.product.entity.ProductSku;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends AbstractIntegrationTest {

    private final String path = "/api/v1/cart";

    @Value("${cart.cookie.name}")
    private String CART_COOKIE;

    @Autowired
    private ShoppingSessionRepo shoppingSessionRepo;

    private ProductSku productSku() {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());
        return list.get(0);
    }

    @Test
    void list_cart_items_anonymous_user() throws Exception {
        this.MOCKMVC
                .perform(get(path)
                        .param("currency", "usd")
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    void create_new_shopping_session() throws Exception {
        MvcResult result = this.MOCKMVC
                .perform(get(path).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = productSku();

        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        var all = this.shoppingSessionRepo.findAll();

        assertFalse(all.isEmpty());
    }

    @Test
    void add_to_existing_shopping_session() throws Exception {
        MvcResult result1 = this.MOCKMVC
                .perform(get(path).with(csrf()))
                .andDo(print())
                .andReturn();

        Cookie cookie1 = result1.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie1);

        var sku = productSku();
        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());

        // method add_to_existing_shopping_session
        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());
    }

}