package com.sarabrandserver.cart.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.payment.projection.TotalPojo;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CartItemRepoTest extends AbstractRepositoryTest {

    @Autowired
    private ShoppingSessionRepo sessionRepo;
    @Autowired
    private CartItemRepo cartItemRepo;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private WorkerProductService service;
    @Autowired
    private ProductSkuRepo skuRepo;

    @Test
    void updateCartQtyByCartId() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 2 ProductSku objects
        service
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(2)
                        ),
                        TestData.files()
                );

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());
        ProductSku sku = skus.getFirst();

        var session = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                new Date(Instant.now().plus(1, HOURS).toEpochMilli()),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        CartItem cart = cartItemRepo.save(new CartItem(sku.getInventory() - 1, session, sku));

        // when
        cartItemRepo.updateCartQtyByCartId(cart.getCartId(), 1);

        Optional<CartItem> optional = cartItemRepo.findById(cart.getCartId());
        assertFalse(optional.isEmpty());

        assertEquals(1, optional.get().getQty());
    }

    @Test
    void deleteCartItemByCookieAndSku() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 2 ProductSku objects
        service
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(2)
                        ),
                        TestData.files()
                );

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());
        ProductSku sku = skus.getFirst();

        var session = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                new Date(Instant.now().plus(1, HOURS).toEpochMilli()),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        CartItem cart = cartItemRepo.save(new CartItem(sku.getInventory() - 1, session, sku));

        // when
        cartItemRepo.deleteCartItemByCookieAndSku(session.getCookie(), sku.getSku());

        Optional<CartItem> optional = cartItemRepo.findById(cart.getCartId());
        assertTrue(optional.isEmpty());
    }

    @Test
    void totalAmountInDefaultCurrency() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        service
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());

        var saved = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                new Date(Instant.now().plus(1, HOURS).toEpochMilli()),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        for (ProductSku sku : skus) {
            cartItemRepo.save(new CartItem(sku.getInventory() - 1, saved, sku));
        }

        // when
        var usd = cartItemRepo.totalAmountInDefaultCurrency(saved.getCookie(), USD);
        var ngn = cartItemRepo.totalAmountInDefaultCurrency(saved.getCookie(), NGN);

        assertFalse(usd.isEmpty());
        assertFalse(ngn.isEmpty());

        for (TotalPojo pojo : usd) {
            assertNotNull(pojo.getQty());
            assertNotNull(pojo.getPrice());
            assertNotNull(pojo.getWeight());
        }

        for (TotalPojo pojo : ngn) {
            assertNotNull(pojo.getQty());
            assertNotNull(pojo.getPrice());
            assertNotNull(pojo.getWeight());
        }
    }

    @Test
    void quantityIsGreaterThanProductSkuInventory() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        // create 3 ProductSku objects
        service
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        var skus = skuRepo.findAll();
        assertEquals(3, skus.size());
        ProductSku sku = skus.getFirst();

        var saved = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                new Date(Instant.now().plus(1, HOURS).toEpochMilli()),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        var cart = cartItemRepo.save(new CartItem(Integer.MAX_VALUE, saved, sku));

        // when
        assertTrue(cart.quantityIsGreaterThanProductSkuInventory());
    }

}