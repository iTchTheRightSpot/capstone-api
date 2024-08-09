package dev.webserver.cart;

import dev.webserver.AbstractIntegration;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}cart")
    private String path;
    @Value("${cart.cookie.name}")
    private String cartcookie;

    @Autowired
    private IShoppingSessionRepository sessionRepository;
    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductSkuRepository productSkuRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void before() {
        var category = categoryRepository.save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 2, workerProductService);

        var clothes = categoryRepository.save(Category.builder().name("clothes").isVisible(true).build());

        TestData.dummyProducts(clothes, 5, workerProductService);
    }

    private ProductSku productSku() {
        var list = TestUtility.toList(productSkuRepository.findAll());
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    @Test
    void listCartItemsAnonymousUser() throws Exception {
        super.mockMvc
                .perform(get(path).param("currency", "usd"))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void createNewShoppingSession() throws Exception {
        MvcResult result = this.mockMvc
                .perform(get(path).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(cartcookie);
        assertNotNull(cookie);

        var sku = productSku();

        var dto = new CartDto(sku.sku(), sku.inventory());

        super.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        var all = TestUtility.toList(sessionRepository.findAll());

        assertFalse(all.isEmpty());
    }

    @Test
    void addToExistingShoppingSession() throws Exception {
        MvcResult result1 = super.mockMvc.perform(get(path).with(csrf())).andReturn();

        Cookie cookie1 = result1.getResponse().getCookie(cartcookie);
        assertNotNull(cookie1);

        var sku = productSku();
        var dto = new CartDto(sku.sku(), sku.inventory());

        super.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());

        // method add_to_existing_shopping_session
        super.mockMvc
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                        .with(csrf())
                        .cookie(cookie1)
                )
                .andExpect(status().isCreated());
    }

}