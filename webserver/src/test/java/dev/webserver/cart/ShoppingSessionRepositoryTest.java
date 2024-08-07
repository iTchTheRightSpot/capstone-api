package dev.webserver.cart;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingSessionRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ShoppingSessionRepository sessionRepo;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepo;

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
        Assertions.assertNotEquals(CustomUtil.toUTC(saved.createAt()), CustomUtil.toUTC(expired));
    }

    @Test
    void cartItemsByCookieValue() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

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
            cartItemRepository.save(new CartItem(sku.getInventory() - 1, saved, sku));
        }

        // when
        var usd = sessionRepo.cartItemsByCookieValue(USD, "cookie");
        var ngn = sessionRepo.cartItemsByCookieValue(NGN, "cookie");

        assertEquals(3, usd.size());
        assertEquals(3, ngn.size());

        for (CartProjection p : usd) {
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
            assertNotNull(p.getWeight());
            assertNotNull(p.getWeightType());
        }

        for (CartProjection p : ngn) {
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
            assertNotNull(p.getWeight());
            assertNotNull(p.getWeightType());
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