package dev.webserver.cart;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.RepositoryTestData;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.payment.CartTotalDbMapper;
import dev.webserver.payment.OrderReservation;
import dev.webserver.payment.OrderReservationRepository;
import dev.webserver.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static dev.webserver.enumeration.CapstoneCurrency.NGN;
import static dev.webserver.enumeration.CapstoneCurrency.USD;
import static dev.webserver.util.CustomUtil.TO_GREENWICH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

final class CartRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private IShoppingSessionRepository sessionRepository;
    @Autowired
    private ICartRepository iCartRepository;
    @Autowired
    private CategoryRepository categoryRepo;
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
    private OrderReservationRepository orderReservationRepository;

    @Test
    void updateCartQtyByCartId() {
        // dummy data
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 2 ProductSku objects
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(2, skus.size());
        final ProductSku sku = skus.getFirst();

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());


        final Cart cart = iCartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        // method to test
        iCartRepository.updateCartQtyByCartId(cart.cartId(), 1);

        final Optional<Cart> optional = iCartRepository.findById(cart.cartId());
        assertFalse(optional.isEmpty());

        assertEquals(1, optional.get().qty());
    }

    @Test
    void deleteCartItemByCookieAndSku() {
        // pre save
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 2 ProductSku objects
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(2, skus.size());
        final ProductSku sku = skus.getFirst();

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        final Cart cart = iCartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        // when
        iCartRepository.deleteCartByCookieAndProductSku(session.cookie(), sku.sku());

        final Optional<Cart> optional = iCartRepository.findById(cart.cartId());
        assertTrue(optional.isEmpty());
    }

    @Test
    void totalAmountInDefaultCurrency() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        for (final ProductSku sku : skus) {
            iCartRepository.save(new Cart(null, sku.inventory(), session.sessionId(), sku.skuId()));
        }

        // method to test
        final var usd = iCartRepository.amountToPayForAllCartItemsForShoppingSession(session.sessionId(), USD);
        final var ngn = iCartRepository.amountToPayForAllCartItemsForShoppingSession(session.sessionId(), NGN);

        assertFalse(ngn.isEmpty());
        assertFalse(usd.isEmpty());

        for (final CartTotalDbMapper pojo : usd) {
            assertNotNull(pojo.qty());
            assertNotNull(pojo.price());
            assertNotNull(pojo.weight());
        }

        for (final CartTotalDbMapper pojo : ngn) {
            assertNotNull(pojo.qty());
            assertNotNull(pojo.price());
            assertNotNull(pojo.weight());
        }
    }

    @Test
    void deleteCartItemsByShoppingSessionId() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());
        final ProductSku first = skus.getFirst();
        final ProductSku second = skus.get(1);
        final ProductSku third = skus.get(2);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        iCartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        iCartRepository.save(new Cart(null, 5, session.sessionId(), second.skuId()));
        iCartRepository.save(new Cart(null, 7, session.sessionId(), third.skuId()));

        assertEquals(3, iCartRepository.cartsByShoppingSessionId(session.sessionId()).size());

        // method to test
        iCartRepository.deleteCartByShoppingSessionId(session.sessionId());

        // then
        assertTrue(iCartRepository.cartsByShoppingSessionId(session.sessionId()).isEmpty());
    }

    @Test
    void cartItemsByShoppingSessionId() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());
        final ProductSku first = skus.getFirst();
        final ProductSku second = skus.get(1);
        final ProductSku third = skus.get(2);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        iCartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        iCartRepository.save(new Cart(null, 5, session.sessionId(), second.skuId()));
        iCartRepository.save(new Cart(null, 7, session.sessionId(), third.skuId()));

        // when
        assertEquals(3, iCartRepository.cartsByShoppingSessionId(session.sessionId()).size());
    }

    @Test
    void cartItemByShoppingSessionIdAndProductSkuSku() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(2, skus.size());
        final ProductSku first = skus.getFirst();
        final ProductSku second = skus.get(1);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        iCartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        iCartRepository.save(new Cart(null, 3, session.sessionId(), second.skuId()));

        // when
        assertFalse(iCartRepository.cartByShoppingSessionIdAndProductSkuSku(session.sessionId(), first.sku()).isEmpty());

        assertFalse(iCartRepository
                .cartByShoppingSessionIdAndProductSkuSku(session.sessionId(), second.sku())
                .isEmpty()
        );
    }

    @Test
    void shouldSuccessfullyRetrieveAllCartsByShoppingSessionId() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());
        ProductSku first = skus.getFirst();
        ProductSku second = skus.get(1);
        ProductSku third = skus.get(2);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        iCartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        iCartRepository.save(new Cart(null, 5, session.sessionId(), second.skuId()));
        iCartRepository.save(new Cart(null, 7, session.sessionId(), third.skuId()));

        // method to test
        final var list = iCartRepository.cartsByShoppingSessionId(session.sessionId());
        assertEquals(3, list.size());

        for (final var pojo : list) {
            assertThat(pojo.skuId()).isNotNull();

            assertThat(pojo.sku()).isNotNull();
            assertThat(pojo.sku().isEmpty()).isFalse();

            assertThat(pojo.inventory()).isNotNull();
            assertThat(pojo.inventory() > 0).isTrue();

            assertThat(pojo.size()).isNotNull();
            assertThat(pojo.size().isEmpty()).isFalse();

            assertThat(pojo.cartId()).isNotNull();

            assertThat(pojo.qty()).isNotNull();
            assertThat(pojo.qty() > 0).isTrue();

            assertThat(pojo.sessionId()).isNotNull();
        }
    }

    @Test
    void shouldReturnCartItemsByOrderReservationReference() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, productPriceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertFalse(skus.isEmpty());

        final var ldt = TO_GREENWICH.apply(null).minusHours(2);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createdAt(ldt)
                .expireAt(ldt.minusHours(1))
                .build());

        final var sku = skus.getFirst();
        orderReservationRepository.save(OrderReservation.builder()
                .reservationId(null)
                .reference("reference-1")
                .qty(sku.inventory() - 1)
                .status(ReservationStatus.PENDING)
                .expireAt(session.expireAt())
                .skuId(sku.skuId())
                .sessionId(session.sessionId())
                .build());

        iCartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        // method to test
        assertEquals(1, iCartRepository.cartIdsByOrderReservationReference("reference-1").size());
    }

}