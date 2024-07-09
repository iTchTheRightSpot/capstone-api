package dev.webserver.cart;

import dev.webserver.AbstractIntegration;
import dev.webserver.category.ProductCategory;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.TestData;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
import dev.webserver.product.WorkerProductService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}cart")
    private String path;
    @Value("${cart.cookie.name}")
    private String CARTCOOKIE;

    @Autowired
    private ShoppingSessionRepository shoppingSessionRepository;
    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductSkuRepository productSkuRepository;
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
        var list = this.productSkuRepository.findAll();
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    @Test
    void listCartItemsAnonymousUser() throws Exception {
        MvcResult result = super.mockMvc
                .perform(get(path)
                        .param("currency", "usd")
                )
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        super.mockMvc
                .perform(asyncDispatch(result))
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void createNewShoppingSession() throws Exception {
        MvcResult result = this.mockMvc
                .perform(get(path).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CARTCOOKIE);
        assertNotNull(cookie);

        var sku = productSku();

        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        super.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        var all = this.shoppingSessionRepository.findAll();

        assertFalse(all.isEmpty());
    }

    @Test
    void addToExistingShoppingSession() throws Exception {
        MvcResult result1 = this.mockMvc
                .perform(get(path).with(csrf()))
                .andReturn();

        Cookie cookie1 = result1.getResponse().getCookie(CARTCOOKIE);
        assertNotNull(cookie1);

        var sku = productSku();
        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        super.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());

        // method add_to_existing_shopping_session
        super.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());
    }

}