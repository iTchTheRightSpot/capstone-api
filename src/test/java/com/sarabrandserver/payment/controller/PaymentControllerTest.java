package com.sarabrandserver.payment.controller;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.enumeration.ShippingType;
import com.sarabrandserver.product.dto.PriceCurrencyDTO;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.shipping.Shipping;
import com.sarabrandserver.shipping.ShippingRepo;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends AbstractIntegrationTest {

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
    private WorkerProductService workerProductService;
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

    @BeforeEach
    void before() {
        shippingRepo.deleteAll();
        productSkuRepo.deleteAll();
        productDetailRepo.deleteAll();
        productRepo.deleteAll();
        categoryRepository.deleteAll();
    }

    private void extracted() {
        shippingRepo.save(new Shipping(new BigDecimal("20000"), new BigDecimal("25.20"), ShippingType.LOCAL));
        shippingRepo
                .save(new Shipping(new BigDecimal("40000.00"), new BigDecimal("30.55"), ShippingType.INTERNATIONAL));

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
        extracted();

        // simulate adding item to cart
        Cookie cookie = create_new_shopping_session();

        // request
        this.MOCKMVC
                .perform(post(path)
                        .param("currency", USD.getCurrency())
                        .param("country", "USA")
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
        extracted();

        var list = this.productSkuRepo.findAll();

        // Retrieve cookie unique to every device
        MvcResult result = this.MOCKMVC
                .perform(get(cartPath).with(csrf()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CART_COOKIE);
        assertNotNull(cookie);

        var sku = list.getFirst();

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
                .perform(post(path)
                        .param("currency", USD.getCurrency())
                        .param("country", "nigeria")
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
                .perform(post(path)
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
        shippingRepo
                .save(new Shipping(new BigDecimal("25025"), new BigDecimal("30.20"), ShippingType.INTERNATIONAL));
        shippingRepo
                .save(new Shipping(new BigDecimal("3075"), new BigDecimal("45.19"), ShippingType.LOCAL));

        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDTO(new BigDecimal("75000"), "NGN"),
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
        TestData
                .dummyProductsTestTotalAmount(
                        category,
                        arr,
                        1, // 1 product
                        1, // variant qty
                        5.5, // 5.5kg
                        workerProductService
                );

        int numOfUsers = 5;

        // numOfUsers add last item to their cart
        Cookie[] cookies = impl(numOfUsers);
        ExecutorService executor = Executors.newFixedThreadPool(numOfUsers);

        CompletableFuture<?>[] futures = new CompletableFuture<?>[numOfUsers];

        for (int i = 0; i < cookies.length; i++) {
            int curr = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    var c = curr % 2 == 0 ? USD.getCurrency() : NGN.getCurrency();
                    var country = curr % 2 == 0 ? "nigeria" : "Canada";
                    return this.MOCKMVC
                            .perform(post(this.path)
                                    .param("currency", c)
                                    .param("country", country)
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

        var skus = productSkuRepo.findAll();
        assertEquals(1, skus.size());
        assertEquals(0, skus.getFirst().getInventory());
    }

    private Cookie createNewShoppingSession() throws Exception {
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
    void validateTotalAmountUSD() throws Exception {
        shippingRepo.save(new Shipping(new BigDecimal("0"), new BigDecimal("30.20"), ShippingType.INTERNATIONAL));

        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal("150.55"), "USD"),
                new PriceCurrencyDTO(new BigDecimal("75000"), "NGN"),
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
                        workerProductService
                );

        Cookie cookie = createNewShoppingSession();

        // math = weight + (price * qty)
        BigDecimal math = BigDecimal.valueOf(weight)
                .add(arr[0].price().multiply(new BigDecimal(qty)));

        BigDecimal total = CustomUtil
                .convertCurrency(
                        usdConversion,
                        USD,
                        // math + shipping cost
                        math.add(new BigDecimal("30.20"))
                ).setScale(2, RoundingMode.CEILING);

        // access payment page
        this.MOCKMVC
                .perform(post(path)
                        .param("currency", USD.getCurrency())
                        .param("country", "Canada")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.total").isNotEmpty());
//                .andExpect(jsonPath("$.total").value(total));
    }

    @Test
    void validateTotalAmountNGN() throws Exception {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDTO(new BigDecimal("75000"), "NGN"),
        };

        shippingRepo.save(new Shipping(new BigDecimal("20000"), new BigDecimal("25.20"), ShippingType.LOCAL));
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
                        workerProductService
                );

        Cookie cookie = createNewShoppingSession();

        // math = weight + (price * qty)
        BigDecimal math = BigDecimal.valueOf(weight)
                .add(arr[1].price().multiply(new BigDecimal(qty)));

        BigDecimal total = CustomUtil
                .convertCurrency(
                        ngnConversion,
                        NGN,
                        math.add(new BigDecimal("20000"))
                );

        // access payment page
        this.MOCKMVC
                .perform(post(path)
                        .param("currency", NGN.getCurrency())
                        .param("country", "nigeria")
                        .with(csrf())
                        .cookie(cookie)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.total").isNotEmpty())
                .andExpect(jsonPath("$.total").value(total));
    }

}