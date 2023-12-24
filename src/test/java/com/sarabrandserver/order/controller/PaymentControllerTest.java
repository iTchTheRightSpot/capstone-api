package com.sarabrandserver.order.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.product.entity.ProductSku;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("Tests against race condition. User doesn't have any item reserved")
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
    Basically user keeps switching from payment to checkout page.
    """)
    void valid() throws Exception {
        var list = this.productSkuRepo.findAll();

        // Retrieve cookie unique to every device
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

        // simulate user in payment component
        this.MOCKMVC
                .perform(get(this.path)
                        .param("currency", USD.getCurrency())
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());

        // simulate returning back to checkout component to update cart
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

        // simulate adding more items
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

    private Cookie[] impl(int num) throws Exception {
        var list = this.productSkuRepo.findAll();

        var sku = list.get(0);

        // update inventory to 1 to simulate num of users trying to purchase the only item in stock
        this.productSkuRepo
                .updateInventoryOnMakingReservation(sku.getSku(), sku.getInventory() - 1);

        Cookie[] cookies = new Cookie[num];

        for (int i = 0; i < num; i++) {
            Cookie cookie = this.MOCKMVC
                    .perform(get(cartPath).with(csrf()))
                    .andReturn()
                    .getResponse()
                    .getCookie(CART_COOKIE);

            assertNotNull(cookie);
            cookies[i] = cookie;
        }

        for (Cookie cookie : cookies) {
            // simulate num of users adding the last item to cart
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
        }

        return cookies;
    }

    @Test
    @DisplayName("""
    Tests against race condition. Multiple users fighting to
    purchase the last item in stock. Only one is going to get 200
    whilst the others get 409.
    """)
    void va() throws Exception {
        int numOfUsers = 5;
        Cookie[] cookies = impl(numOfUsers);
        ExecutorService executor = Executors.newFixedThreadPool(numOfUsers);

        CompletableFuture<?>[] futures = new CompletableFuture<?>[numOfUsers];

        for (int i = 0; i < cookies.length; i++) {
            int curr = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    var c = curr % 2 == 0 ? USD.getCurrency() : NGN.getCurrency();
                    return this.MOCKMVC
                            .perform(get(this.path)
                                    .param("currency", c)
                                    .with(csrf())
                                    .cookie(cookies[curr])
                            )
                            .andReturn();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // Wait for all CompletableFuture to complete
        CompletableFuture.allOf(futures).get();
        List<Integer> results = new ArrayList<>();

        for (CompletableFuture<?> future : futures) {
            MvcResult join = (MvcResult) future.join();
            results.add(join.getResponse().getStatus());
        }

        assertEquals(1, Collections.frequency(results, 200));
        assertEquals(numOfUsers - 1, Collections.frequency(results, 409));
    }

}