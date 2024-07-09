package dev.webserver.cart;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.ProductCategory;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.payment.OrderReservation;
import dev.webserver.payment.TotalProjection;
import dev.webserver.payment.OrderReservationRepository;
import dev.webserver.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;

class CartItemRepositoryTest extends AbstractRepositoryTest {

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
    @Autowired
    private OrderReservationRepository orderReservationRepository;

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
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

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

        CartItem cart = cartItemRepository.save(new CartItem(sku.getInventory() - 1, session, sku));

        // when
        cartItemRepository.updateCartItemQtyByCartId(cart.getCartId(), 1);

        Optional<CartItem> optional = cartItemRepository.findById(cart.getCartId());
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
        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

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

        CartItem cart = cartItemRepository.save(new CartItem(sku.getInventory() - 1, session, sku));

        // when
        cartItemRepository.deleteCartItemByCookieAndSku(session.cookie(), sku.getSku());

        Optional<CartItem> optional = cartItemRepository.findById(cart.getCartId());
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
            cartItemRepository.save(new CartItem(sku.getInventory(), saved, sku));
        }

        // when
        var usd = cartItemRepository.amountToPayForAllCartItemsForShoppingSession(saved.shoppingSessionId(), USD);
        var ngn = cartItemRepository.amountToPayForAllCartItemsForShoppingSession(saved.shoppingSessionId(), NGN);

        assertFalse(ngn.isEmpty());
        assertFalse(usd.isEmpty());

        for (TotalProjection pojo : usd) {
            assertNotNull(pojo.getQty());
            assertNotNull(pojo.getPrice());
            assertNotNull(pojo.getWeight());
        }

        for (TotalProjection pojo : ngn) {
            assertNotNull(pojo.getQty());
            assertNotNull(pojo.getPrice());
            assertNotNull(pojo.getWeight());
        }
    }

    @Test
    void deleteCartItemsByShoppingSessionId() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
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
        ProductSku first = skus.getFirst();
        ProductSku second = skus.get(1);
        ProductSku third = skus.get(2);

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

        cartItemRepository.save(new CartItem(3, session, first));
        cartItemRepository.save(new CartItem(5, session, second));
        cartItemRepository.save(new CartItem(7, session, third));

        assertEquals(3, cartItemRepository
                .cartItemsByShoppingSessionId(session.shoppingSessionId()).size()
        );

        // when
        cartItemRepository
                .deleteCartItemsByShoppingSessionId(session.shoppingSessionId());

        // then
        assertTrue(cartItemRepository
                .cartItemsByShoppingSessionId(session.shoppingSessionId()).isEmpty()
        );
    }

    @Test
    void cartItemsByShoppingSessionId() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
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
        ProductSku first = skus.getFirst();
        ProductSku second = skus.get(1);
        ProductSku third = skus.get(2);

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

        cartItemRepository.save(new CartItem(3, session, first));
        cartItemRepository.save(new CartItem(5, session, second));
        cartItemRepository.save(new CartItem(7, session, third));

        // when
        assertEquals(3,
                cartItemRepository
                        .cartItemsByShoppingSessionId(session.shoppingSessionId())
                        .size()
        );
    }

    @Test
    void cartItemByShoppingSessionIdAndProductSkuSku() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());
        ProductSku first = skus.getFirst();
        ProductSku second = skus.get(1);

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

        cartItemRepository.save(new CartItem(3, session, first));
        cartItemRepository.save(new CartItem(3, session, second));

        // when
        assertFalse(cartItemRepository
                .cartItemByShoppingSessionIdAndProductSkuSku(
                        session.shoppingSessionId(),
                        first.getSku()
                )
                .isEmpty()
        );

        assertFalse(cartItemRepository
                .cartItemByShoppingSessionIdAndProductSkuSku(
                        session.shoppingSessionId(),
                        second.getSku()
                )
                .isEmpty()
        );
    }

    @Test
    void shouldSuccessfullyRetrieveRaceConditionCartPojo() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
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
        ProductSku first = skus.getFirst();
        ProductSku second = skus.get(1);
        ProductSku third = skus.get(2);

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

        cartItemRepository.save(new CartItem(3, session, first));
        cartItemRepository.save(new CartItem(5, session, second));
        cartItemRepository.save(new CartItem(7, session, third));

        // when
        var list = cartItemRepository
                .cartItemsByShoppingSessionId(session.shoppingSessionId());
        assertEquals(3, list.size());

        for (var pojo : list) {
            assertTrue(pojo.getProductSkuId() > 0);
            assertFalse(pojo.getProductSkuSku().isEmpty());
            assertFalse(pojo.getProductSkuSize().isEmpty());
            assertTrue(pojo.getProductSkuInventory() > 0);
            assertTrue(pojo.getCartItemId() > 0);
            assertTrue(pojo.getCartItemQty() > 0);
            assertTrue(pojo.getShoppingSessionId() > 0);
        }
    }

    @Test
    void shouldReturnCartItemsByOrderReservationReference() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var createExpired = new Date(Instant.now().minus(2, HOURS).toEpochMilli());
        var toExpire = new Date(Instant.now().minus(1, HOURS).toEpochMilli());

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());

        var session = this.sessionRepo.save(new ShoppingSession(
                "cookie",
                createExpired,
                toExpire,
                new HashSet<>(),
                new HashSet<>()
        ));

        var sku = skus.getFirst();
        orderReservationRepository.save(new OrderReservation(
                "reference-1",
                sku.getInventory() - 1,
                ReservationStatus.PENDING,
                toExpire,
                sku,
                session
        ));
        cartItemRepository.save(new CartItem(sku.getInventory() - 1, session, sku));

        // method to test
        assertEquals(1, cartItemRepository.cartItemsByOrderReservationReference("reference-1").size());
    }

}