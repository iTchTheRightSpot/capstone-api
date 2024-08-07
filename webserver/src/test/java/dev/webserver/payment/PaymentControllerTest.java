package dev.webserver.payment;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.cart.CartDto;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.TestData;
import dev.webserver.product.PriceCurrencyDto;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
import dev.webserver.product.WorkerProductService;
import dev.webserver.shipping.ShipSetting;
import dev.webserver.shipping.ShippingRepository;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}payment")
    private String path;
    @Value(value = "/${api.endpoint.baseurl}cart")
    private String cartPath;
    @Value("${cart.cookie.name}")
    private String CART_COOKIE;
    @Value("${sarre.usd.to.cent}")
    private String usdConversion;
    @Value("${sarre.ngn.to.kobo}")
    private String ngnConversion;

    @Autowired
    private WorkerProductService productService;
    @Autowired
    private ProductSkuRepository productSkuRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ShippingRepository shippingRepository;

    private void preSaveNecessaryData() {
        shippingRepository
                .save(new ShipSetting(
                        "Canada",
                        new BigDecimal("20000"),
                        new BigDecimal("25.20")
                ));
        shippingRepository
                .save(new ShipSetting(
                        "Nigeria",
                        new BigDecimal("40000.00"),
                        new BigDecimal("30.55")
                ));

        var category = categoryRepository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 2, productService);

        var clothes = categoryRepository
                .save(
                        Category.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(clothes, 5, productService);
    }

    private ProductSku productSku() {
        var list = this.productSkuRepository.findAll();
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    private Cookie createNewShoppingSessionCookie() throws Exception {
        MvcResult result = this.mockMvc
                .perform(get(cartPath).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = productSku();

        super.mockMvc
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(this.mapper
                                .writeValueAsString(new CartDto(sku.getSku(), sku.getInventory()))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        return cookie;
    }

    @Test
    void testRaceConditionWhereAUserDoesNotChangeAnythingInCart() throws Exception {
        preSaveNecessaryData();

        // simulate adding item to cart
        Cookie cookie = createNewShoppingSessionCookie();

        // request
        super.mockMvc
                .perform(post(path)
                        .param("currency", USD.getCurrency())
                        .param("country", "USA")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());
    }

    @Test
    void raceConditionWhereAUserIsIndecisiveAboutQtyInTheirCart() throws Exception {
        preSaveNecessaryData();

        var list = this.productSkuRepository.findAll();

        // Retrieve cookie unique to every device
        MvcResult result = super.mockMvc
                .perform(get(cartPath).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = list.getFirst();

        // simulate adding to cart
        super.mockMvc
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper
                                .writeValueAsString(new CartDto(sku.getSku(), sku.getInventory() - 1))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        // simulate user in payment component
        super.mockMvc
                .perform(post(path)
                        .param("currency", USD.getCurrency())
                        .param("country", "nigeria")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());

        // simulate returning back to checkout component to update cart
        super.mockMvc
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper
                                .writeValueAsString(new CartDto(sku.getSku(), 1))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        // simulate adding more items
        for (int i = 1; i < list.size(); i++) {
            ProductSku s = list.get(i);
            super.mockMvc
                    .perform(post(cartPath)
                            .contentType(APPLICATION_JSON)
                            .content(super.mapper
                                    .writeValueAsString(new CartDto(s.getSku(), s.getInventory()))
                            )
                            .with(csrf())
                            .cookie(cookie)
                    )
                    .andExpect(status().isCreated());
        }

        // simulate user switching to pay in ngn
        super.mockMvc
                .perform(post(path)
                        .param("currency", NGN.getCurrency())
                        .param("country", "nigeria")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());
    }

    private Cookie[] impl(int num) throws Exception {
        var list = this.productSkuRepository.findAll();

        var sku = list.getFirst();

        Cookie[] cookies = new Cookie[num];

        for (int i = 0; i < num; i++) {
            Cookie cookie = super.mockMvc
                    .perform(get(cartPath).with(csrf()))
                    .andReturn()
                    .getResponse()
                    .getCookie(CART_COOKIE);

            assertNotNull(cookie);
            cookies[i] = cookie;
        }

        for (Cookie cookie : cookies) {
            // simulate num of users adding the last item to cart
            super.mockMvc
                    .perform(post(cartPath)
                            .contentType(APPLICATION_JSON)
                            .content(super.mapper
                                    .writeValueAsString(new CartDto(sku.getSku(), 1))
                            )
                            .with(csrf())
                            .cookie(cookie)
                    )
                    .andExpect(status().isCreated());
        }

        return cookies;
    }

    @Test
    void multipleUserTryPurchasingTheLastItemButOnlyOneUserOrRequestIsSuccessfully() throws Exception {
        shippingRepository
                .save(new ShipSetting(
                        new Faker().country().name(),
                        new BigDecimal("25025"),
                        new BigDecimal("30.20")
                ));
        shippingRepository
                .save(new ShipSetting(
                        new Faker().country().name(),
                        new BigDecimal("3075"),
                        new BigDecimal("45.19")
                ));

        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal("75000"), "NGN"),
        };

        var category = categoryRepository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        // create a product of one variant where inventory is 1
        TestData.dummyProductsTestTotalAmount(
                category,
                arr,
                1, // 1 product
                1, // variant qty
                5.5, // 5.5kg
                productService
        );

        int numOfUsers = 5;

        // numOfUsers add last item to their cart
        Cookie[] cookies = impl(numOfUsers);

        List<Integer> results = CustomUtil.asynchronousTasks(getSuppliers(cookies)).join()
                .stream()
                .map(result -> result.getResponse().getStatus())
                .toList();

        assertEquals(1, Collections.frequency(results, 200));
        assertEquals(numOfUsers - 1, Collections.frequency(results, 409));

        var skus = productSkuRepository.findAll();
        assertEquals(1, skus.size());
        assertEquals(0, skus.getFirst().getInventory());
    }

    private @NotNull List<Supplier<MvcResult>> getSuppliers(Cookie[] cookies) {
        List<Supplier<MvcResult>> asyncSuppliers = new ArrayList<>();

        for (int i = 0; i < cookies.length; i++) {
            var c = i % 2 == 0 ? USD.getCurrency() : NGN.getCurrency();
            var country = i % 2 == 0 ? "nigeria" : "Canada";
            int finalI = i;
            asyncSuppliers.add(() -> {
                try {
                    return super.mockMvc
                            .perform(post(path)
                                    .param("currency", c)
                                    .param("country", country)
                                    .with(csrf())
                                    .cookie(cookies[finalI])
                            )
                            .andReturn();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return asyncSuppliers;
    }

    private Cookie createNewShoppingSession() throws Exception {
        MvcResult result = super.mockMvc
                .perform(get(cartPath).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = productSku();

        super.mockMvc
                .perform(post(cartPath)
                        .contentType(APPLICATION_JSON)
                        .content(this.mapper
                                .writeValueAsString(new CartDto(sku.getSku(), sku.getInventory()))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        return cookie;
    }

    @Test
    void validateTotalAmountUSD() throws Exception {
        shippingRepository
                .save(new ShipSetting(
                        "Canada",
                        new BigDecimal("55000"),
                        new BigDecimal("30.20")
                ));

        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal("150.55"), "USD"),
                new PriceCurrencyDto(new BigDecimal("75000"), "NGN"),
        };

        var category = categoryRepository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        int qty = 1;
        double weight = 2.5;

        TestData
                .dummyProductsTestTotalAmount(
                        category,
                        arr,
                        1, // 1 product
                        qty, // 1 variants
                        weight, // 2.5kg
                        productService
                );

        Cookie cookie = createNewShoppingSession();

        BigDecimal total = CustomUtil
                .convertCurrency(
                        usdConversion,
                        USD,
                        // math + shipping cost
                        arr[0].price()
                                .multiply(new BigDecimal(qty))
                                .add(new BigDecimal("30.20"))
                );

        super.mockMvc
                .perform(post(path)
                        .param("currency", USD.getCurrency())
                        .param("country", "Canada")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.total").isNotEmpty())
                // because trailing zero is removed
                // https://stackoverflow.com/questions/70076401/assert-bigdecimal-with-two-trailing-zeros-using-mockito
                .andExpect(jsonPath("$.total").value(total.doubleValue()));
    }

    @Test
    void validateTotalAmountNGN() throws Exception {
        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal("75000"), "NGN"),
        };

        shippingRepository
                .save(new ShipSetting(
                        "nigeria",
                        new BigDecimal("20000"),
                        new BigDecimal("25.20")
                ));

        var category = categoryRepository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        int qty = 5;
        double weight = 5.5;
        TestData
                .dummyProductsTestTotalAmount(
                        category,
                        arr,
                        1, // 1 product
                        qty, // 5 variants
                        weight, // 5.5kg
                        productService
                );

        Cookie cookie = createNewShoppingSession();

        BigDecimal total = CustomUtil
                .convertCurrency(
                        ngnConversion,
                        NGN,
                        arr[1].price()
                                .multiply(new BigDecimal(qty))
                                .add(new BigDecimal("20000"))
                );

        super.mockMvc
                .perform(post(path)
                        .param("currency", NGN.getCurrency())
                        .param("country", "nigeria")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.total").isNotEmpty())
                // because trailing zero is removed
                // https://stackoverflow.com/questions/70076401/assert-bigdecimal-with-two-trailing-zeros-using-mockito
                .andExpect(jsonPath("$.total").value(total.doubleValue()));
    }

}