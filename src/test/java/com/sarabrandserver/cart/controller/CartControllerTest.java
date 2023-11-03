package com.sarabrandserver.cart.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.cart.service.CartService;
import com.sarabrandserver.product.entity.ProductSku;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class CartControllerTest extends AbstractIntegrationTest {

    private final String path = "/api/v1/client/cart";

    @Autowired private ShoppingSessionRepo shoppingSessionRepo;
    @Autowired private CartItemRepo cartItemRepo;
    @Autowired private CartService cartService;

    @BeforeEach
    void setUp() {
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            log.info("Custom IP address {}", ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        var sku = productSku();
        var dto = new CartDTO(sku.getSku(), sku.getInventory());
        this.cartService
                .create_new_shopping_session(ip, dto);
    }

    @AfterEach
    void tearDown() {
        this.shoppingSessionRepo.deleteAll();
    }

    private ProductSku productSku() {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());
        return list.get(0);
    }

    @Test
    void listCartItems() throws Exception {
        this.MOCKMVC
                .perform(get(path).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void list_cart_items_anonymous_user() throws Exception {
        this.MOCKMVC
                .perform(get(path).param("currency", "usd").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void create_new_shopping_session() throws Exception {
        var sku = productSku();

        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

    @Test
    void add_to_existing_shopping_session() throws Exception {
        var sku = productSku();
        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

}