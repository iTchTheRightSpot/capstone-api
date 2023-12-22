package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.order.projection.TotalPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    @Query(value = """
    SELECT c
    FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    WHERE s.shoppingSessionId = :id AND c.sku = :sku
    """)
    Optional<CartItem> cart_item_by_shopping_session_id_and_sku(long id, String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CartItem c SET c.qty = :qty WHERE c.cartId = :id")
    void updateCartQtyByCartId(long id, int qty);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.shoppingSession.cookie = :cookie AND c.sku = :sku")
    void delete_cartItem_by_cookie_and_sku(String cookie, String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.shoppingSession.shoppingSessionId = :id")
    void deleteByParentID(long id);

    @Query("""
    SELECT c
    FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    WHERE s.cookie = :cookie
    """)
    List<CartItem> cart_items_by_shopping_session_cookie(String cookie);

    @Query("""
    SELECT
    c.qty AS qty,
    pc.price as price
    FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    INNER JOIN ProductSku ps ON c.sku = ps.sku
    INNER JOIN ProductDetail d ON ps.productDetail.productDetailId = d.productDetailId
    INNER JOIN Product p ON d.product.productId = p.productId
    INNER JOIN PriceCurrency pc ON p.productId = pc.product.productId
    WHERE s.cookie = :cookie AND pc.currency = :currency
    """)
    List<TotalPojo> total_amount_in_default_currency(String cookie, SarreCurrency currency);

}