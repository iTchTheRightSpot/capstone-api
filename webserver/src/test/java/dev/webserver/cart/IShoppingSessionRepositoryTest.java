package dev.webserver.cart;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static dev.webserver.util.CustomUtil.TO_GREENWICH;
import static org.junit.jupiter.api.Assertions.*;

class IShoppingSessionRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private IShoppingSessionRepository sessionRepository;
    @Autowired
    private ICartRepository iCartRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductSkuRepository skuRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepository;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepository;

    @Test
    void shoppingSessionByCookie() {
        // given
        final var ldt = TO_GREENWICH.apply(null);
        sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        // method to test and assert
        assertFalse(sessionRepository.shoppingSessionByCookie("cookie").isEmpty());
    }

    @Test
    void updateShoppingSessionExpiryTime() {
        // given
        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        final var update = session.expireAt().plusHours(2);
        // method to test
        sessionRepository.updateShoppingSessionExpiry("cookie", update);

        // when
        Assertions.assertNotEquals(session.expireAt(), update);
    }

    @Test
    void cartItemsByCookieValue() {
        // given
        final var cat = categoryRepository.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        for (final ProductSku sku : skus) {
            iCartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));
        }

        // when
        final var usd = sessionRepository.cartItemsByCookieValue(USD, "cookie");
        final var ngn = sessionRepository.cartItemsByCookieValue(NGN, "cookie");

        assertEquals(3, usd.size());
        assertEquals(3, ngn.size());

        for (final CartDbMapper p : usd) {
            assertNotNull(p.uuid());
            assertNotNull(p.sessionId());
            assertNotNull(p.imageKey());
            assertNotNull(p.name());
            assertNotNull(p.currency());
            assertEquals(USD, p.currency());
            assertNotNull(p.price());
            assertNotNull(p.colour());
            assertNotNull(p.size());
            assertNotNull(p.sku());
            assertNotNull(p.qty());
            assertNotNull(p.weight());
            assertNotNull(p.weightType());
        }

        for (final CartDbMapper p : ngn) {
            assertNotNull(p.uuid());
            assertNotNull(p.sessionId());
            assertNotNull(p.imageKey());
            assertNotNull(p.name());
            assertNotNull(p.currency());
            assertEquals(NGN, p.currency());
            assertNotNull(p.price());
            assertNotNull(p.colour());
            assertNotNull(p.size());
            assertNotNull(p.sku());
            assertNotNull(p.qty());
            assertNotNull(p.weight());
            assertNotNull(p.weightType());
        }
    }

    @Test
    void allExpiredShoppingSession() {
        // given
        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null).minusHours(2);
        final LocalDateTime expire = ldt.minusHours(1);

        int num = 5;

        for (int i = 0; i < num; i++) {
            sessionRepository.save(ShoppingSession.builder()
                    .cookie("cookie" + i)
                    .createAt(ldt)
                    .expireAt(expire)
                    .build());
        }

        // when
        assertEquals(num, sessionRepository.allExpiredShoppingSession(expire).size());
    }

}