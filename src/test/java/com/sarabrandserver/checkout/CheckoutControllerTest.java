package com.sarabrandserver.checkout;

import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CheckoutControllerTest extends AbstractIntegration {

    @Value("${api.endpoint.baseurl}checkout")
    private String path;
    @Value(value = "/${api.endpoint.baseurl}cart")
    private String cartPath;
    @Value("${cart.cookie.name}")
    private String cookie;

    @Autowired
    private WorkerProductService service;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductSkuRepo productSkuRepo;

    @Test
    void checkoutShouldThrowNotFoundError() throws Exception {
        super.mockMvc
                .perform(get("/" + path)
                        .with(csrf())
                        .param("country", "nigeria")
                        .param("currency", "usd")
                )
                .andExpect(status().isNotFound());
    }

    private ProductSku productSku() {
        var category = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 5, service);

        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    private Cookie createNewShoppingSessionCookie() throws Exception {
        MvcResult result = super.mockMvc
                .perform(get(cartPath).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(this.cookie);
        assertNotNull(cookie);

        var sku = productSku();

        this.mockMvc
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(super.objectMapper
                                .writeValueAsString(new CartDTO(sku.getSku(), sku.getInventory()))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        return cookie;
    }

    @Test
    void shouldSuccessfullyReturnCheckoutDetailsWithPrincipalPropertyEmpty() throws Exception {
        // given
        Cookie cooke = createNewShoppingSessionCookie();

        // when
        super.mockMvc
                .perform(get("/" + path)
                        .with(csrf())
                        .param("country", "nigeria")
                        .param("currency", "usd")
                        .cookie(cooke)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").isEmpty());
    }

    @Test
    @WithMockUser(username = "client@client.com", password = "password", roles = {"CLIENT"})
    void shouldSuccessfullyReturnCheckoutDetailsWithPrincipalPropertyNotEmpty() throws Exception {
        // given
        Cookie cooke = createNewShoppingSessionCookie();

        // when
        super.mockMvc
                .perform(get("/" + path)
                        .with(csrf())
                        .param("country", "nigeria")
                        .param("currency", "usd")
                        .cookie(cooke)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value("client@client.com"));
    }

}