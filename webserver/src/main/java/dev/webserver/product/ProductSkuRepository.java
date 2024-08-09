package dev.webserver.product;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProductSkuRepository extends CrudRepository<ProductSku, Long> {

    /**
     * Retrieves a {@link ProductSku} based on the provided sku.
     *
     * @param sku The property of a {@link ProductSku}.
     * @return An {@link Optional} containing the matching {@link ProductSku},
     *         or empty if no {@link ProductSku} is found with the provided sku.
     */
    @Query(value = "SELECT * FROM product_sku p WHERE p.sku = :sku")
    Optional<ProductSku> productSkuBySku(String sku);

    /**
     * Update a {@link ProductSku} qty property by deducting from current qty.
     *
     * @param sku is a unique string for every {@link ProductSku}.
     * @param qty is the number to add to an existing {@link ProductSku}.
     * */
    @Transactional
    @Modifying
    @Query("UPDATE product_sku s SET s.inventory = (s.inventory - :qty) WHERE s.sku = :sku")
    void updateProductSkuInventoryBySubtractingFromExistingInventory(String sku, int qty);

    /**
     * Updates a {@link ProductSku} inventory property by adding qty in parameter to
     * {@link ProductSku} inventory.
     *
     * @param sku is a unique string for every {@link ProductSku}.
     * @param qty the number to add to an existing {@link ProductSku} inventory.
     * */
    @Transactional
    @Modifying
    @Query("UPDATE product_sku s SET s.inventory = (s.inventory + :qty) WHERE s.sku = :sku")
    void updateProductSkuInventoryByAddingToExistingInventory(String sku, int qty);

    /**
     * Deletes a {@link ProductSku} by its property sku.
     * */
    @Transactional
    @Modifying
    @Query("DELETE FROM product_sku s WHERE s.sku = :sku")
    void deleteProductSkuBySku(String sku);

    /**
     * Retrieves a {@link Product} based on a {@link ProductSku} sku property.
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
    SELECT
        *
    FROM product p
    INNER JOIN product_detail d ON p.product_id = d.product_id
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE s.sku = :sku
    """)
    Optional<Product> productByProductSku(String sku);

}
