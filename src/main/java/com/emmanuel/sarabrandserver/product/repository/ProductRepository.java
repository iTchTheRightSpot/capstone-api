package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.projection.ClientProductPojo;
import com.emmanuel.sarabrandserver.product.projection.DetailPojo;
import com.emmanuel.sarabrandserver.product.projection.ProductPojo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p WHERE p.name = :name")
    Optional<Product> findByProductName(@Param(value = "name") String name);

    @Query(value = """
    SELECT
    p.productId AS id,
    p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    p.defaultKey AS key
    FROM Product p
    """)
    List<ProductPojo> fetchAllProductsWorker(Pageable pageable);

    @Query(value = """
    SELECT p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    pd.sku AS sku,
    ps.size AS size,
    inv.quantity AS quantity,
    pc.colour AS colour,
    GROUP_CONCAT(DISTINCT (img.imageKey)) AS image
    FROM Product p
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductSize ps ON pd.productSize.productSizeId = ps.productSizeId
    INNER JOIN ProductInventory inv ON pd.productInventory.productInventoryId = inv.productInventoryId
    INNER JOIN ProductImage img ON pd.productDetailId = img.productDetails.productDetailId
    INNER JOIN ProductColour pc ON pd.productColour.productColourId = pc.productColourId
    WHERE pd.isVisible = true
    group by p.name, p.description, p.price, p.currency, pd.sku, ps.size, pc.colour
    """)
    List<ClientProductPojo> fetchAllProductsClient(Pageable pageable);

    @Query(value = """
    SELECT p FROM Product p
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    WHERE p.name = :name AND pd.sku = :sku
    """)
    Optional<Product> findByProductNameAndSku(@Param(value = "name") String name, @Param(value = "sku") String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ProductDetail pd WHERE pd.sku = :sku")
    void custom_delete(@Param(value = "sku") String name);

    @Query(value = "SELECT det FROM ProductDetail det WHERE det.sku = :sku")
    Optional<ProductDetail> findDetailBySku(@Param(value = "sku") String sku);

    @Query(value = "SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findProductByProductId(@Param(value = "id") long id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
    UPDATE Product p
    SET p.name = :name, p.description = :desc, p.price = :price
    WHERE p.productId = : id
    """)
    void updateProduct(
            @Param(value = "id") long id,
            @Param(value = "name") String name,
            @Param(value = "desc") String desc,
            @Param(value = "price") BigDecimal price
    );

    @Query(value = """
    SELECT
    pd.sku AS sku,
    pd.isVisible AS visible,
    ps.size AS size,
    inv.quantity AS qty,
    pc.colour AS colour,
    GROUP_CONCAT(DISTINCT (img.imageKey)) AS key
    FROM ProductDetail pd
    INNER JOIN Product p ON pd.product.productId = p.productId
    INNER JOIN ProductSize ps ON pd.productSize.productSizeId = ps.productSizeId
    INNER JOIN ProductInventory inv ON pd.productInventory.productInventoryId = inv.productInventoryId
    INNER JOIN ProductImage img ON pd.productDetailId = img.productDetails.productDetailId
    INNER JOIN ProductColour pc ON pd.productColour.productColourId = pc.productColourId
    WHERE p.name = :name
    GROUP BY pd.sku, ps.size, pc.colour
    """)
    List<DetailPojo> findDetailByProductNameWorker(@Param(value = "name") String name, Pageable page);

}
