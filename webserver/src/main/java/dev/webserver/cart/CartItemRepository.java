package dev.webserver.cart;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.payment.RaceConditionCartProjection;
import dev.webserver.payment.TotalProjection;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends CrudRepository<CartItem, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE CartItem c SET c.qty = :qty WHERE c.cartId = :id")
    void updateCartItemQtyByCartId(long id, int qty);

    @Transactional
    @Modifying
    @Query("""
    DELETE
        c.*
    FROM cart_item c
    INNER JOIN product_sku s ON s.sku_id = c.sku_id
    INNER JOIN shopping_session sh ON sh.session_id = c.session_id
    WHERE sh.cookie = :cookie AND s.sku = :sku
    """)
    void deleteCartItemByCookieAndSku(String cookie, String sku);

    @Transactional
    @Modifying
    @Query("DELETE c.* FROM cart_item c WHERE c.session_id = :id")
    void deleteCartItemsByShoppingSessionId(long id);

    @Query("""
    SELECT
    c.qty AS qty,
    pc.price AS price,
    p.weight AS weight
    FROM cart_item c
    INNER JOIN shopping_session s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    INNER JOIN product_sku ps ON c.productSku.skuId = ps.skuId
    INNER JOIN product_detail d ON ps.productDetail.productDetailId = d.productDetailId
    INNER JOIN product p ON d.product.productId = p.productId
    INNER JOIN price_currency pc ON p.productId = pc.product.productId
    WHERE s.session_id = :sessionId AND pc.currency = :currency
    """)
    List<TotalProjection> amountToPayForAllCartItemsForShoppingSession(long sessionId, SarreCurrency currency);

    @Query("""
    SELECT
    p.skuId AS productSkuId,
    p.sku AS productSkuSku,
    p.size AS productSkuSize,
    p.inventory AS productSkuInventory,
    c.cartId AS cartItemId,
    c.qty AS cartItemQty,
    s.shoppingSessionId AS shoppingSessionId
    FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    INNER JOIN ProductSku p ON c.productSku.skuId = p.skuId
    WHERE s.shoppingSessionId = :id
    """)
    List<RaceConditionCartProjection> cartItemsByShoppingSessionId(long id);

    @Query("""
    SELECT
    c.cartId AS cartItemId
    FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    INNER JOIN OrderReservation o ON s.shoppingSessionId = o.shoppingSession.shoppingSessionId
    WHERE o.reference = :reference
    """)
    List<RaceConditionCartProjection> cartItemsByOrderReservationReference(String reference);

    @Query("""
    SELECT c FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    INNER JOIN ProductSku sk ON c.productSku.skuId = sk.skuId
    WHERE s.shoppingSessionId = :id AND sk.sku = :sku
    """)
    Optional<CartItem> cartItemByShoppingSessionIdAndProductSkuSku(long id, String sku);

}