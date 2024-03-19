package dev.webserver.cart.repository;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.cart.entity.CartItem;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.category.entity.ProductCategory;
import dev.webserver.category.repository.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.payment.projection.TotalPojo;
import dev.webserver.product.entity.ProductSku;
import dev.webserver.product.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
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
    private ProductSkuRepo skuRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;
    @Autowired
    private ProductImageRepo imageRepo;

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
                .createProduct(2, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

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
        cartItemRepo.updateCartItemQtyByCartId(cart.getCartId(), 1);

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
        RepositoryTestData
                .createProduct(2, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

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
        cartItemRepo.deleteCartItemByCookieAndSku(session.cookie(), sku.getSku());

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

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

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
            cartItemRepo.save(new CartItem(sku.getInventory(), saved, sku));
        }

        // when
        var usd = cartItemRepo.amountToPayForAllCartItemsForShoppingSession(saved.shoppingSessionId(), USD);
        var ngn = cartItemRepo.amountToPayForAllCartItemsForShoppingSession(saved.shoppingSessionId(), NGN);

        assertFalse(ngn.isEmpty());
        assertFalse(usd.isEmpty());

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
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

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
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

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

        cartItemRepo.save(new CartItem(3, session, first));
        cartItemRepo.save(new CartItem(5, session, second));
        cartItemRepo.save(new CartItem(7, session, third));

        assertEquals(3, cartItemRepo
                .cartItemsByShoppingSessionId(session.shoppingSessionId()).size()
        );

        // when
        cartItemRepo
                .deleteCartItemsByShoppingSessionId(session.shoppingSessionId());

        // then
        assertTrue(cartItemRepo
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
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

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

        cartItemRepo.save(new CartItem(3, session, first));
        cartItemRepo.save(new CartItem(5, session, second));
        cartItemRepo.save(new CartItem(7, session, third));

        // when
        assertEquals(3,
                cartItemRepo
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
                .createProduct(2, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

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

        cartItemRepo.save(new CartItem(3, session, first));
        cartItemRepo.save(new CartItem(3, session, second));

        // when
        assertFalse(cartItemRepo
                .cartItemByShoppingSessionIdAndProductSkuSku(
                        session.shoppingSessionId(),
                        first.getSku()
                )
                .isEmpty()
        );

        assertFalse(cartItemRepo
                .cartItemByShoppingSessionIdAndProductSkuSku(
                        session.shoppingSessionId(),
                        second.getSku()
                )
                .isEmpty()
        );
    }

    @Test
    void deleteCartItemByCartItemId() {
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
                .createProduct(2, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertEquals(2, skus.size());

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

        long item = cartItemRepo.save(new CartItem(3, session, skus.getFirst())).getCartId();

        // when
        cartItemRepo.deleteCartItemByCartItemId(item);

        // then
        assertTrue(cartItemRepo.findById(item).isEmpty());
    }

}