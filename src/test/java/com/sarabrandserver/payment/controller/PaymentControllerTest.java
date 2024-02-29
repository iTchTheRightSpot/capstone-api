package com.sarabrandserver.payment.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.payment.repository.OrderDetailRepository;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.product.dto.PriceCurrencyDto;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.shipping.entity.ShipSetting;
import com.sarabrandserver.shipping.repository.ShippingRepo;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    private ProductRepo productRepo;
    @Autowired
    private ProductSkuRepo productSkuRepo;
    @Autowired
    private ProductDetailRepo productDetailRepo;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ShippingRepo shippingRepo;
    @Autowired
    private OrderDetailRepository detailRepository;
    @Autowired
    private OrderReservationRepo reservationRepo;
    @Autowired
    private CartItemRepo cartItemRepo;

    @BeforeEach
    void before() {
        detailRepository.deleteAll();
        reservationRepo.deleteAll();
        cartItemRepo.deleteAll();
        shippingRepo.deleteAll();
        productSkuRepo.deleteAll();
        productDetailRepo.deleteAll();
        productRepo.deleteAll();
        categoryRepository.deleteAll();

        // as we are deleting all from Shipping,
        // we need to save default object again
        shippingRepo
                .save(new ShipSetting("default", new BigDecimal("0.00"), new BigDecimal("0.00")));
    }

    private void preSaveNecessaryData() {
        shippingRepo
                .save(new ShipSetting(
                        "Canada",
                        new BigDecimal("20000"),
                        new BigDecimal("25.20")
                ));
        shippingRepo
                .save(new ShipSetting(
                        "Nigeria",
                        new BigDecimal("40000.00"),
                        new BigDecimal("30.55")
                ));

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

        TestData.dummyProducts(category, 2, productService);

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

        TestData.dummyProducts(clothes, 5, productService);
    }

    private ProductSku productSku() {
        var list = this.productSkuRepo.findAll();
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
                        .content(this.objectMapper
                                .writeValueAsString(new CartDTO(sku.getSku(), sku.getInventory()))
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
                .perform(get(path)
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

        var list = this.productSkuRepo.findAll();

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
                        .content(super.objectMapper
                                .writeValueAsString(new CartDTO(sku.getSku(), sku.getInventory() - 1))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        // simulate user in payment component
        super.mockMvc
                .perform(get(path)
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
                        .content(super.objectMapper
                                .writeValueAsString(new CartDTO(sku.getSku(), 1))
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
                            .content(super.objectMapper
                                    .writeValueAsString(new CartDTO(s.getSku(), s.getInventory()))
                            )
                            .with(csrf())
                            .cookie(cookie)
                    )
                    .andExpect(status().isCreated());
        }

        // simulate user switching to pay in ngn
        super.mockMvc
                .perform(get(path)
                        .param("currency", NGN.getCurrency())
                        .param("country", "nigeria")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isOk());
    }

    private Cookie[] impl(int num) throws Exception {
        var list = this.productSkuRepo.findAll();

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
                            .content(super.objectMapper
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
    void multipleUserTryPurchasingTheLastItemButOnlyIsAllowedAndTheRestGetA409() throws Exception {
        shippingRepo
                .save(new ShipSetting(
                        new Faker().country().name(),
                        new BigDecimal("25025"),
                        new BigDecimal("30.20")
                ));
        shippingRepo
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
                        ProductCategory.builder()
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

        List<CompletableFuture<MvcResult>> futures = new ArrayList<>();

        for (int i = 0; i < cookies.length; i++) {
            final int curr = i;
            futures.add(
                    CompletableFuture.supplyAsync(() -> {
                                try {
                                    var c = curr % 2 == 0 ? USD.getCurrency() : NGN.getCurrency();
                                    var country = curr % 2 == 0 ? "nigeria" : "Canada";
                                    return super.mockMvc
                                            .perform(get(path)
                                                    .param("currency", c)
                                                    .param("country", country)
                                                    .with(csrf())
                                                    .cookie(cookies[curr])
                                            )
                                            .andReturn();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
        }

        // complete all CompletableFuture
        CustomUtil.asynchronousTasks(futures);

        List<Integer> results = new ArrayList<>();

        for (CompletableFuture<MvcResult> future : futures) {
            results.add(future.join().getResponse().getStatus());
        }

        assertEquals(1, Collections.frequency(results, 200));
        assertEquals(numOfUsers - 1, Collections.frequency(results, 409));

        var skus = productSkuRepo.findAll();
        assertEquals(1, skus.size());
        assertEquals(0, skus.getFirst().getInventory());
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
                        .content(this.objectMapper
                                .writeValueAsString(new CartDTO(sku.getSku(), sku.getInventory()))
                        )
                        .with(csrf())
                        .cookie(cookie)
                )
                .andExpect(status().isCreated());

        return cookie;
    }

    @Test
    void validateTotalAmountUSD() throws Exception {
        shippingRepo
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
                        ProductCategory.builder()
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
                .perform(get(path)
                        .param("currency", USD.getCurrency())
                        .param("country", "Canada")
                        .cookie(cookie)
                )
                .andDo(print())
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

        shippingRepo
                .save(new ShipSetting(
                        "nigeria",
                        new BigDecimal("20000"),
                        new BigDecimal("25.20")
                ));

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
                .perform(get(path)
                        .param("currency", NGN.getCurrency())
                        .param("country", "nigeria")
                        .cookie(cookie)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.total").isNotEmpty())
                // because trailing zero is removed
                // https://stackoverflow.com/questions/70076401/assert-bigdecimal-with-two-trailing-zeros-using-mockito
                .andExpect(jsonPath("$.total").value(total.doubleValue()));
    }

}