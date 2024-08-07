package dev.webserver.cart;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.payment.CartTotalDbMapper;
import dev.webserver.payment.OrderReservation;
import dev.webserver.payment.OrderReservationRepository;
import dev.webserver.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static dev.webserver.util.CustomUtil.TO_GREENWICH;
import static org.junit.jupiter.api.Assertions.*;

class CartRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private IShoppingSessionRepository sessionRepository;
    @Autowired
    private ICartRepository ICartRepository;
    @Autowired
    private CategoryRepository categoryRepo;
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
    @Autowired
    private OrderReservationRepository orderReservationRepository;

    @Test
    void updateCartQtyByCartId() {
        // dummy data
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 2 ProductSku objects
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(2, skus.size());
        final ProductSku sku = skus.getFirst();

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());


        final Cart cart = ICartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        // method to test
        ICartRepository.updateCartQtyByCartId(cart.cartId(), 1);

        final Optional<Cart> optional = ICartRepository.findById(cart.cartId());
        assertFalse(optional.isEmpty());

        assertEquals(1, optional.get().qty());
    }

    @Test
    void deleteCartItemByCookieAndSku() {
        // pre save
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        // create 2 ProductSku objects
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(2, skus.size());
        final ProductSku sku = skus.getFirst();

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        final Cart cart = ICartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        // when
        ICartRepository.deleteCartByCookieAndProductSku(session.cookie(), sku.sku());

        final Optional<Cart> optional = ICartRepository.findById(cart.cartId());
        assertTrue(optional.isEmpty());
    }

    @Test
    void totalAmountInDefaultCurrency() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

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
            ICartRepository.save(new Cart(null, sku.inventory(), session.sessionId(), sku.skuId()));
        }

        // method to test
        final var usd = ICartRepository.amountToPayForAllCartItemsForShoppingSession(session.sessionId(), USD);
        final var ngn = ICartRepository.amountToPayForAllCartItemsForShoppingSession(session.sessionId(), NGN);

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
                .createProduct(3, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());
        final ProductSku first = skus.getFirst();
        final ProductSku second = skus.get(1);
        final ProductSku third = skus.get(2);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        ICartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        ICartRepository.save(new Cart(null, 5, session.sessionId(), second.skuId()));
        ICartRepository.save(new Cart(null, 7, session.sessionId(), third.skuId()));

        assertEquals(3, ICartRepository.cartByShoppingSessionId(session.sessionId()).size());

        // method to test
        ICartRepository.deleteCartByShoppingSessionId(session.sessionId());

        // then
        assertTrue(ICartRepository.cartByShoppingSessionId(session.sessionId()).isEmpty());
    }

    @Test
    void cartItemsByShoppingSessionId() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());
        final ProductSku first = skus.getFirst();
        final ProductSku second = skus.get(1);
        final ProductSku third = skus.get(2);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        ICartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        ICartRepository.save(new Cart(null, 5, session.sessionId(), second.skuId()));
        ICartRepository.save(new Cart(null, 7, session.sessionId(), third.skuId()));

        // when
        assertEquals(3, ICartRepository.cartByShoppingSessionId(session.sessionId()).size());
    }

    @Test
    void cartItemByShoppingSessionIdAndProductSkuSku() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(2, skus.size());
        final ProductSku first = skus.getFirst();
        final ProductSku second = skus.get(1);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        ICartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        ICartRepository.save(new Cart(null, 3, session.sessionId(), second.skuId()));

        // when
        assertFalse(ICartRepository.cartByShoppingSessionIdAndProductSkuSku(session.sessionId(), first.sku()).isEmpty());

        assertFalse(ICartRepository
                .cartByShoppingSessionIdAndProductSkuSku(session.sessionId(), second.sku())
                .isEmpty()
        );
    }

    @Test
    void shouldSuccessfullyRetrieveRaceConditionCartPojo() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertEquals(3, skus.size());
        ProductSku first = skus.getFirst();
        ProductSku second = skus.get(1);
        ProductSku third = skus.get(2);

        final var ldt = TO_GREENWICH.apply(null);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
                .expireAt(ldt.plusHours(1))
                .build());

        ICartRepository.save(new Cart(null, 3, session.sessionId(), first.skuId()));
        ICartRepository.save(new Cart(null, 5, session.sessionId(), second.skuId()));
        ICartRepository.save(new Cart(null, 7, session.sessionId(), third.skuId()));

        // method to test
        final var list = ICartRepository.cartByShoppingSessionId(session.sessionId());
        assertEquals(3, list.size());

        for (final var pojo : list) {
            assertTrue(pojo.skuId() > 0);
            assertFalse(pojo.sku().isEmpty());
            assertFalse(pojo.size().isEmpty());
            assertTrue(pojo.inventory() > 0);
            assertTrue(pojo.cartId() > 0);
            assertTrue(pojo.qty() > 0);
            assertTrue(pojo.sessionId() > 0);
        }
    }

    @Test
    void shouldReturnCartItemsByOrderReservationReference() {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepository, priceCurrencyRepository, imageRepository, skuRepository);

        final var skus = TestUtility.toList(skuRepository.findAll());
        assertFalse(skus.isEmpty());

        final var ldt = TO_GREENWICH.apply(null).minusHours(2);
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie("cookie")
                .createAt(ldt)
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

        ICartRepository.save(new Cart(null, sku.inventory() - 1, session.sessionId(), sku.skuId()));

        // method to test
        assertEquals(1, ICartRepository.cartIdsByOrderReservationReference("reference-1").size());
    }

}