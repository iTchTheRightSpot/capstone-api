package com.sarabrandserver.cart.controller;

import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CartControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}cart")
    private String path;
    @Value("${cart.cookie.name}")
    private String CART_COOKIE;

    @Autowired
    private ShoppingSessionRepo shoppingSessionRepo;
    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductSkuRepo productSkuRepo;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void before() {
        var category = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 2, workerProductService);

        var clothes = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(clothes, 5, workerProductService);
    }

    private ProductSku productSku() {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    @Test
    void list_cart_items_anonymous_user() throws Exception {
        this.mockMvc
                .perform(get(path)
                        .param("currency", "usd")
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    void create_new_shopping_session() throws Exception {
        MvcResult result = this.mockMvc
                .perform(get(path).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = productSku();

        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        var all = this.shoppingSessionRepo.findAll();

        assertFalse(all.isEmpty());
    }

    @Test
    void add_to_existing_shopping_session() throws Exception {
        MvcResult result1 = this.mockMvc
                .perform(get(path).with(csrf()))
                .andDo(print())
                .andReturn();

        Cookie cookie1 = result1.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie1);

        var sku = productSku();
        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());

        // method add_to_existing_shopping_session
        this.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());
    }

}