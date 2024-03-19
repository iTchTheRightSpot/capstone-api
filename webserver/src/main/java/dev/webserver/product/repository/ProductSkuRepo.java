package dev.webserver.product.repository;

import dev.webserver.product.entity.Product;
import dev.webserver.product.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProductSkuRepo extends JpaRepository<ProductSku, Long> {

    /**
     * Retrieves a {@link ProductSku} based on the provided sku.
     *
     * @param sku The property of a {@link ProductSku}.
     * @return An {@link Optional} containing the matching {@link ProductSku},
     *         or empty if no {@link ProductSku} is found with the provided sku.
     */
    @Query(value = "SELECT p FROM ProductSku p WHERE p.sku = :sku")
    Optional<ProductSku> productSkuBySku(String sku);

    /**
     * Update a {@link ProductSku} qty property by deducting from current qty.
     *
     * @param sku is a unique string for every {@link ProductSku}.
     * @param qty is the number to add to an existing {@link ProductSku}.
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
     * Updates a {@link ProductSku} qty property by adding from current qty.
     *
     * @param sku is a unique string for every {@link ProductSku}.
     * @param qty is the number to add to an existing {@link ProductSku}.
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
     * Deletes a {@link ProductSku} by its property 'sku'.
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ProductSku s WHERE s.sku = :sku")
    void deleteProductSkuBySku(String sku);

    /**
     * Retrieves a {@link Product} based on the provided sku.
     * <p>
     * This method returns a {@link Product} entity by querying
     * its relationship with a {@link ProductSku}. It filters
     * products based on the sku associated with the {@link ProductSku}.
     *
     * @param sku The property of a {@link ProductSku}.
     * @return An {@link Optional} containing the matching {@link Product},
     *         or empty if no {@link Product} is found with the provided sku.
     */
    @Query("""
    SELECT p FROM Product p
    INNER JOIN ProductDetail d ON p.productId = d.product.productId
    INNER JOIN ProductSku s ON d.productDetailId = s.productDetail.productDetailId
    WHERE s.sku = :sku
    """)
    Optional<Product> productByProductSku(String sku);

}
