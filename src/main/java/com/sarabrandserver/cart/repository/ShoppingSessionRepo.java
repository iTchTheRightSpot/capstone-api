package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.projection.CartPojo;
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

    @Query("SELECT s FROM ShoppingSession s WHERE s.shoppingSessionId = :id")
    Optional<ShoppingSession> shoppingSessionById(Long id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
    UPDATE ShoppingSession s
    SET s.expireAt = :d
    WHERE s.shoppingSessionId = :id
    """)
    void updateSessionExpiry(long id, Date d);

    @Query(value = """
    SELECT
    p.name AS name,
    s.shoppingSessionId AS session,
    p.defaultKey AS key,
    ps.sku AS sku,
    c.qty AS qty
    FROM ShoppingSession s
    INNER JOIN SarreBrandUser u ON s.sarreBrandUser.email = u.email
    INNER JOIN CartItem c ON s.shoppingSessionId = c.shoppingSession.shoppingSessionId
    INNER JOIN ProductSku ps ON ps.sku = c.sku
    INNER JOIN ProductDetail d ON ps.productDetail.productDetailId = d.productDetailId
    INNER JOIN Product p ON d.product.productId = p.productId
    WHERE u.email = :principal
    """)
    List<CartPojo> cartItemsByPrincipal(String principal);

}