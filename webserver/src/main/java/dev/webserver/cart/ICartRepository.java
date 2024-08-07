package dev.webserver.cart;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.payment.RaceConditionCartDbMapper;
import dev.webserver.payment.CartTotalDbMapper;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ICartRepository extends CrudRepository<Cart, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE cart SET qty = :qty WHERE cartId = :cartId")
    void updateCartQtyByCartId(long cartId, int qty);

    @Transactional
    @Modifying
    @Query("""
    DELETE c.* FROM cart c
    INNER JOIN product_sku s ON s.sku_id = c.sku_id
    INNER JOIN shopping_session sh ON sh.session_id = c.session_id
    WHERE sh.cookie = :cookie AND s.sku = :sku
    """)
    void deleteCartByCookieAndProductSku(String cookie, String sku);

    @Transactional
    @Modifying
    @Query("DELETE c.* FROM cart c WHERE c.session_id = :sessionId")
    void deleteCartByShoppingSessionId(long sessionId);

    @Query("""
    SELECT
    c.qty AS qty,
    pc.price AS price,
    p.weight AS weight
    FROM cart c
    INNER JOIN product_sku ps ON c.sku_id = ps.sku_id
    INNER JOIN product_detail d ON ps.detail_id = d.detail_id
    INNER JOIN product p ON d.product_id = p.product_id
    INNER JOIN price_currency pc ON p.product_id = pc.product_id
    WHERE c.session_id = :sessionId AND pc.currency = :currency
    """)
    List<CartTotalDbMapper> amountToPayForAllCartItemsForShoppingSession(long sessionId, SarreCurrency currency);

    @Query("""
    SELECT
        p.sku_id AS skuId,
        p.sku AS sku,
        p.size AS size,
        p.inventory AS inventory,
        c.cart_id AS cartId,
        c.qty AS qty,
        c.session_id AS sessionId
    FROM cart c
    INNER JOIN product_sku p ON c.sku_id = p.sku_id
    WHERE c.session_id = :sessionId
    """)
    List<RaceConditionCartDbMapper> cartByShoppingSessionId(long sessionId);

    @Query("""
    SELECT
        c.cartId
    FROM cart c
    INNER JOIN shopping_session s ON c.session_id = s.session_id
    INNER JOIN order_reservation o ON s.session_id = o.session_id
    WHERE o.reference = :reference
    """)
    List<Long> cartIdsByOrderReservationReference(String reference);

    @Query("""
    SELECT * FROM cart c
    INNER JOIN product_sku p ON c.sku_id = p.sku_id
    WHERE c.session_id = :sessionId AND p.sku = :sku
    """)
    Optional<Cart> cartByShoppingSessionIdAndProductSkuSku(long sessionId, String sku);

}