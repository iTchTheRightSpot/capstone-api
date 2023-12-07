package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.projection.CartPojo;
import com.sarabrandserver.enumeration.SarreCurrency;
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
    s.shoppingSessionId AS session,
    p.defaultKey AS key,
    p.name AS name,
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS currency,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS price,
    d.colour AS colour,
    ps.size AS size,
    ps.sku AS sku,
    c.qty AS qty
    FROM ShoppingSession s
    INNER JOIN CartItem c ON s.shoppingSessionId = c.shoppingSession.shoppingSessionId
    INNER JOIN ProductSku ps ON ps.sku = c.sku
    INNER JOIN ProductDetail d ON ps.productDetail.productDetailId = d.productDetailId
    INNER JOIN Product p ON d.product.productId = p.productId
    WHERE s.cookie = :cookie
    """)
    List<CartPojo> cartItemsByCookieValue(SarreCurrency currency, String cookie);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    DELETE FROM ShoppingSession s
    WHERE s.expireAt <= :d
    """)
    void deleteIfExpired(Date d);

}