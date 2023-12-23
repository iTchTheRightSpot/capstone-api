package com.sarabrandserver.order.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.product.entity.ProductSku;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
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

    @Test
    @DisplayName("""
    Tests against race condition. User being indecisive with the qty.
    Basically a keeps switching from payment page back to adding new items to cart
    """)
    void valid() throws Exception {
        var list = this.productSkuRepo.findAll();

        MvcResult result = this.MOCKMVC
                .perform(get(cartPath).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = list.get(0);

        // simulate adding to cart
        this.MOCKMVC
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER
                                .writeValueAsString(new CartDTO(sku.getSku(), sku.getInventory() - 1))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        // simulate going to checkout view
        this.MOCKMVC
                .perform(get(this.path)
                        .param("currency", USD.getCurrency())
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());

        // simulate deducting item in cart qty
        this.MOCKMVC
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER
                                .writeValueAsString(new CartDTO(sku.getSku(), 1))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        // simulate add
        for (int i = 1; i < list.size(); i++) {
            ProductSku s = list.get(i);
            this.MOCKMVC
                    .perform(post(cartPath)
                            .contentType(APPLICATION_JSON)
                            .content(this.MAPPER
                                    .writeValueAsString(new CartDTO(s.getSku(), s.getInventory()))
                            )
                            .with(csrf())
                            .cookie(cookie)
                    )
                    .andExpect(status().isCreated());
        }

        // simulate user switching to pay in ngn
        this.MOCKMVC
                .perform(get(this.path)
                        .param("currency", NGN.getCurrency())
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("""
    Tests against race condition. Final check before payment page
    is shown when Product is out of stock.
    """)
    void va() throws Exception {
        Cookie cookie = this.MOCKMVC
                .perform(get(cartPath).with(csrf()))
                .andReturn()
                .getResponse()
                .getCookie(CART_COOKIE);

        Cookie cookie2 = this.MOCKMVC
                .perform(get(cartPath).with(csrf()))
                .andReturn()
                .getResponse()
                .getCookie(CART_COOKIE);

        assertNotNull(cookie);
        assertNotNull(cookie2);

        var list = this.productSkuRepo.findAll();

        var sku = list.get(0);

        // simulate adding to cart
        this.MOCKMVC
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER
                                .writeValueAsString(new CartDTO(sku.getSku(), sku.getInventory() - 1))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        // simulate going to checkout view
        this.MOCKMVC
                .perform(get(this.path)
                        .param("currency", USD.getCurrency())
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());

        // simulate adding to cart
        this.MOCKMVC
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER
                                .writeValueAsString(new CartDTO(sku.getSku(), 1))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isConflict());
    }

}