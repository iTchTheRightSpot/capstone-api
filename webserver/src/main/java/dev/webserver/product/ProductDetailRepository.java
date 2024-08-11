package dev.webserver.product;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductDetailRepository extends CrudRepository<ProductDetail, Long> {

    /**
     * Returns a {@link ProductDetail} by {@link ProductSku} property sku
     * */
    @Query(value = """
    SELECT d.* FROM product_detail d
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE s.sku = :sku
    """)
    Optional<ProductDetail> productDetailByProductSku(final String sku);

    @Query("SELECT * FROM product_detail WHERE colour = :colour")
    Optional<ProductDetail> productDetailByColour(final String colour);

    /**
     * using native MySQL query, method updates a {@link ProductDetail} and {@link ProductSku}.
     */
    @Transactional
    @Modifying
    @Query(value = """
    UPDATE product_sku s
    INNER JOIN product_detail d ON d.detail_id = s.detail_id
    SET d.colour = :colour, d.is_visible = :visible, s.inventory = :qty, s.size = :size
    WHERE s.sku = :sku
    """)
    void updateProductSkuAndProductDetailByProductSku(
            final String sku,
            final String colour,
            final boolean visible,
            final int qty,
            final String size
    );

    /**
     * Query retrieves all {@link ProductDetail}s associated to a {@link Product} by its uuid.
     * It filters and maps using Spring Data Projection.
     * The magic is since {@link ProductDetail} has a 1 to many relationship with
     * {@link ProductImage} and {@link ProductSku}, we are getting all {@link ProductImage}s
     * keys (comma separated). These keys we use to retrieve pre-assigned urls from s3.
     * Finally, for {@link ProductSku} we retrieve an array of distinct custom objects.
     * NOTE: this method is similar to findProductDetailsByProductUuidWorker only it
     * filters by {@link ProductDetail} being visible.
     */
    @Query(value = """
    SELECT
            d.is_visible AS is_visible,
    d.colour AS colour,
            GROUP_CONCAT(DISTINCT i.image_key) AS image_key,
    CONCAT('[',
        GROUP_CONCAT(
            DISTINCT JSON_OBJECT(
                'sku', s.sku,
                'inventory', IF(s.inventory > 0, 0, -1),
                'size', s.size
            )
        ),
    ']') AS variants
    FROM product_detail d
    INNER JOIN product_image i ON d.detail_id = i.detail_id
    INNER JOIN product p ON d.product_id = p.product_id
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE p.uuid = :uuid AND d.is_visible = true
    GROUP BY d.is_visible, d.colour
    """)
    List<ProductDetailDbMapper> productDetailsByProductUuidClientFront(final String uuid);

    @Query(value = """
    SELECT
            d.is_visible AS is_visible,
    d.colour AS colour,
            GROUP_CONCAT(DISTINCT i.image_key) AS image_key,
    CONCAT('[',
        GROUP_CONCAT(
            DISTINCT JSON_OBJECT(
                'sku', s.sku,
                'inventory', s.inventory,
                'size', s.size
            )
        ),
    ']') AS variants
    FROM product_detail d
    INNER JOIN product_image i ON d.detail_id = i.detail_id
    INNER JOIN product p ON d.product_id = p.product_id
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE p.uuid = :uuid
    GROUP BY d.is_visible, d.colour
    """)
    List<ProductDetailDbMapper> productDetailsByProductUuidAdminFront(final String uuid);

}
