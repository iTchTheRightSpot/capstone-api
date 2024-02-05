package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.payment.projection.TotalPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CartItem c SET c.qty = :qty WHERE c.cartId = :id")
    void updateCartQtyByCartId(long id, int qty);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.shoppingSession.cookie = :cookie AND c.productSku.sku = :sku")
    void deleteCartItemByCookieAndSku(String cookie, String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.shoppingSession.shoppingSessionId = :id")
    void deleteByParentId(long id);

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
    WHERE s.cookie = :cookie AND pc.currency = :currency
    """)
    List<TotalPojo> totalAmountInDefaultCurrency(String cookie, SarreCurrency currency);

}