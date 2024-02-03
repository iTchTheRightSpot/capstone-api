package com.sarabrandserver.product.repository;

import com.sarabrandserver.product.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProductSkuRepo extends JpaRepository<ProductSku, Long> {

    @Query(value = "SELECT p FROM ProductSku p WHERE p.sku = :sku")
    Optional<ProductSku> findBySku(String sku);

    @Query("""
    SELECT COUNT (s.skuId)
    FROM ProductSku s
    INNER JOIN OrderDetail o ON s.skuId = o.sku.skuId
    WHERE s.sku = :sku
    """)
    int skuHasBeenPurchased(String sku);

    @Query("""
    SELECT COUNT (s.skuId)
    FROM ProductSku s
    INNER JOIN CartItem c ON s.skuId = c.productSku.skuId
    WHERE s.sku = :sku
    """)
    int skuContainsInUserCart(String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE ProductSku s
    SET
    s.inventory = (s.inventory - :qty)
    WHERE s.sku = :sku
    """)
    void updateInventoryOnMakingReservation(String sku, int qty);

    /**
     * Update ProductSku inventory based on ProductSku.sku.
     * Achieves this by adding qty passed in the parameter.
     * Note this method is only called in PaymentService when
     * reserved ProductSku has either expired or user no longer
     * wants to purchase item.
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE ProductSku s
    SET
    s.inventory = (s.inventory + :qty)
    WHERE s.sku = :sku
    """)
    void incrementInventory(String sku, int qty);

}
