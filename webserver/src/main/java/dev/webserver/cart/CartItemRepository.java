package dev.webserver.cart;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.payment.RaceConditionCartProjection;
import dev.webserver.payment.TotalProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CartItem c SET c.qty = :qty WHERE c.cartId = :id")
    void updateCartItemQtyByCartId(long id, int qty);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.shoppingSession.cookie = :cookie AND c.productSku.sku = :sku")
    void deleteCartItemByCookieAndSku(String cookie, String sku);

    /**
     * Deletes all {@link CartItem} associated to a
     * {@link ShoppingSession} categoryId.
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.shoppingSession.shoppingSessionId = :id")
    void deleteCartItemsByShoppingSessionId(long id);

    /**
     * @param sessionId is associated to every device.
     * @return List of {@link TotalProjection} which are select items needed
     * to calculate the total in a users cart. The select items are
     * qty, price, and weight.
     * */
    @Query("""
    SELECT
    c.qty AS qty,
    pc.price AS price,
    p.weight AS weight
    FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    INNER JOIN ProductSku ps ON c.productSku.skuId = ps.skuId
    INNER JOIN ProductDetail d ON ps.productDetail.productDetailId = d.productDetailId
    INNER JOIN Product p ON d.product.productId = p.productId
    INNER JOIN PriceCurrency pc ON p.productId = pc.product.productId
    WHERE s.shoppingSessionId = :sessionId AND pc.currency = :currency
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