package com.sarabrandserver.cart.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.projection.CartPojo;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ShoppingSessionRepoTest extends AbstractRepositoryTest {

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
    void shoppingSessionByCookie() {
        // given
        this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                new Date(Instant.now().plus(1, HOURS).toEpochMilli()),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        // when
        assertFalse(this.sessionRepo.shoppingSessionByCookie("cookie").isEmpty());
    }

    @Test
    void updateShoppingSessionExpiryTime() {
        // given
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

        var instant = Instant.now().plus(2, HOURS);
        var expired = Date.from(instant);
        sessionRepo.updateShoppingSessionExpiry("cookie", expired);

        // when
        var session = this.sessionRepo.findById(saved.shoppingSessionId());
        assertFalse(session.isEmpty());
        assertNotEquals(CustomUtil.toUTC(saved.createAt()), CustomUtil.toUTC(expired));
    }

    @Test
    void cartItemsByCookieValue() {
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
        var usd = sessionRepo.cartItemsByCookieValue(USD, "cookie");
        var ngn = sessionRepo.cartItemsByCookieValue(NGN, "cookie");

        assertEquals(3, usd.size());
        assertEquals(3, ngn.size());

        for (CartPojo p : usd) {
            assertNotNull(p.getUuid());
            assertNotNull(p.getSession());
            assertNotNull(p.getKey());
            assertNotNull(p.getName());
            assertNotNull(p.getCurrency());
            assertEquals(USD, p.getCurrency());
            assertNotNull(p.getPrice());
            assertNotNull(p.getColour());
            assertNotNull(p.getSize());
            assertNotNull(p.getSku());
            assertNotNull(p.getQty());
        }

        for (CartPojo p : ngn) {
            assertNotNull(p.getUuid());
            assertNotNull(p.getSession());
            assertNotNull(p.getKey());
            assertNotNull(p.getName());
            assertNotNull(p.getCurrency());
            assertEquals(NGN, p.getCurrency());
            assertNotNull(p.getPrice());
            assertNotNull(p.getColour());
            assertNotNull(p.getSize());
            assertNotNull(p.getSku());
            assertNotNull(p.getQty());
        }
    }

    @Test
    void allExpiredShoppingSession() {
        // given
        var createExpired = new Date(Instant.now().minus(2, HOURS).toEpochMilli());
        var toExpire = new Date(Instant.now().minus(1, HOURS).toEpochMilli());

        int num = 5;

        for (int i = 0; i < num; i++) {
            this.sessionRepo
                    .save(
                            new ShoppingSession(
                                    "cookie" + i,
                                    createExpired,
                                    toExpire,
                                    new HashSet<>(),
                                    new HashSet<>()
                            )
                    );
        }

        // when
        assertEquals(num, sessionRepo.allExpiredShoppingSession(new Date()).size());
    }

}