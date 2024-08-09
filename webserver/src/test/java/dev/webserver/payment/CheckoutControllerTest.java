package dev.webserver.payment;

import dev.webserver.AbstractIntegration;
import dev.webserver.TestUtility;
import dev.webserver.cart.CartDto;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.TestData;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
import dev.webserver.product.WorkerProductService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private ProductSkuRepository productSkuRepository;

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
                .save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 5, service);

        var list = TestUtility.toList(productSkuRepository.findAll());
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
                        .content(super.mapper.writeValueAsString(new CartDto(sku.sku(), sku.inventory())))
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