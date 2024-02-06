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

    /**
     * Update a {@code ProductSku} qty property by deducting from current qty.
     *
     * @param sku is a unique string for every {@code ProductSku}.
     * @param qty is the number to add to an existing {@code ProductSku}.
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE ProductSku s
    SET
    s.inventory = (s.inventory - :qty)
    WHERE s.sku = :sku
    """)
    void updateProductSkuInventoryBySubtractingFromExistingInventory(String sku, int qty);

    /**
     * Update a {@code ProductSku} qty property by adding from current qty.
     *
     * @param sku is a unique string for every {@code ProductSku}.
     * @param qty is the number to add to an existing {@code ProductSku}.
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE ProductSku s
    SET
    s.inventory = (s.inventory + :qty)
    WHERE s.sku = :sku
    """)
    void updateProductSkuInventoryByAddingToExistingInventory(String sku, int qty);

    /**
     * Deletes ProductSku from db
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ProductSku s WHERE s.sku = :sku")
    void deleteProductSkuBySku(String sku);

}
