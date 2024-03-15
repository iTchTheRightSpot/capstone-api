package dev.capstone.cart.repository;

import dev.capstone.cart.entity.ShoppingSession;
import dev.capstone.cart.projection.CartPojo;
import dev.capstone.enumeration.SarreCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingSessionRepo extends JpaRepository<ShoppingSession, Long> {

    @Query("SELECT s FROM ShoppingSession s WHERE s.cookie = :cookie")
    Optional<ShoppingSession> shoppingSessionByCookie(String cookie);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
    UPDATE ShoppingSession s
    SET s.expireAt = :d
    WHERE s.cookie = :cookie
    """)
    void updateShoppingSessionExpiry(String cookie, Date d);

    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.defaultKey AS key,
    p.name AS name,
    p.weight AS weight,
    p.weightType AS weightType,
    s.shoppingSessionId AS session,
    cur.currency AS currency,
    cur.price AS price,
    d.colour AS colour,
    ps.size AS size,
    ps.sku AS sku,
    c.qty AS qty
    FROM ShoppingSession s
    INNER JOIN CartItem c ON s.shoppingSessionId = c.shoppingSession.shoppingSessionId
    INNER JOIN ProductSku ps ON c.productSku.skuId = ps.skuId
    INNER JOIN ProductDetail d ON ps.productDetail.productDetailId = d.productDetailId
    INNER JOIN Product p ON d.product.productId = p.productId
    INNER JOIN PriceCurrency cur ON p.productId = cur.product.productId
    WHERE s.cookie = :cookie AND cur.currency = :currency
    GROUP BY p.uuid, s.shoppingSessionId, p.defaultKey, p.name, cur.currency, cur.price, d.colour, ps.size, ps.sku, c.qty
    """)
    List<CartPojo> cartItemsByCookieValue(SarreCurrency currency, String cookie);

    @Query("SELECT s FROM ShoppingSession s WHERE s.expireAt <= :d")
    List<ShoppingSession> allExpiredShoppingSession(Date d);

}