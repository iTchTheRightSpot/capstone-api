package dev.webserver.cart;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.RepositoryTestData;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDateTime;
import java.util.List;

import static dev.webserver.enumeration.CapstoneCurrency.NGN;
import static dev.webserver.enumeration.CapstoneCurrency.USD;
import static dev.webserver.util.CustomUtil.TO_GREENWICH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

final class IShoppingSessionRepositoryTest extends AbstractRepositoryTest {

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
    private ProductPriceCurrencyRepository productPriceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepository;
    @Autowired
    private JdbcClient client;

    @Test
    void shoppingSessionByCookie() {
        // given
        final var ldt = TO_GREENWICH.apply(null);
        sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
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
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        // method to test
        sessionRepository.updateShoppingSessionExpiry("cookie", session.expireAt().plusDays(1));

        // when
        assertThat(session.expireAt().plusDays(1).toLocalDate())
                .isEqualTo(sessionRepository.findById(session.sessionId()).orElseThrow().expireAt().toLocalDate());
    }

    private List<ProductSku> skus(final long productDetailId) {
        return client.sql("SELECT * FROM product_sku WHERE detail_id = :id")
                .param("id", productDetailId)
                .query(ProductSku.class)
                .list();
    }

    private List<ProductDetail> details(final long productId) {
        return client.sql("SELECT * FROM product_detail WHERE product_id = :id")
                .param("id", productId)
                .query(ProductDetail.class)
                .list();
    }

    private void updateVisibility (final long productDetailId) {
        client.sql("UPDATE product_detail SET is_visible = FALSE WHERE detail_id = :id")
                .param("id", productDetailId)
                .update();
    }

    @Test
    void cartItemsByCookieValue() {
        // given
        final var cat = categoryRepository.save(Category.builder().name("category").isVisible(true).build());

        for (int i = 0; i < 3; i++)
            RepositoryTestData
                    .createProductAndMultipleDetails(3, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var products = TestUtility.toList(productRepository.findAll());
        assertEquals(3, products.size());

        final var details = details(products.getFirst().productId());
        assertEquals(3, details.size());

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        for (final ProductSku sku : skus(details.getFirst().detailId()))
            iCartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        for (final ProductSku sku : skus(details.get(1).detailId()))
            iCartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        // method to test and assert
        final var usd = sessionRepository.cartItemsByCookieValue(USD, "cookie");

        assertFalse(usd.isEmpty());

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

        updateVisibility(details.get(1).detailId());

        // method to test and assert
        final var ngn = sessionRepository.cartItemsByCookieValue(NGN, "cookie");
        assertFalse(ngn.isEmpty());

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
        final LocalDateTime ldt = CustomUtil.TO_GREENWICH.apply(null).minusMinutes(10);

        int num = 5;

        for (int i = 0; i < num; i++)
            sessionRepository.save(
                    ShoppingSession.builder()
                            .cookie("cookie" + i)
                            .createdAt(ldt)
                            .expireAt(ldt.minusHours(i))
                            .build());

        // when
        assertEquals(num, sessionRepository.allExpiredShoppingSession(ldt.plusMinutes(10)).size());
    }

}