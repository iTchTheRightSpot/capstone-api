package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.projection.DetailPojo;
import com.emmanuel.sarabrandserver.product.projection.Imagez;
import com.emmanuel.sarabrandserver.product.projection.ProductPojo;
import org.springframework.data.domain.Page;
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

/** All methods ending with worker are for admin dashboard. Client is the opposite */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p WHERE p.name = :name")
    Optional<Product> findByProductName(@Param(value = "name") String name);

    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    p.defaultKey AS key
    FROM Product p
    """)
    Page<ProductPojo> fetchAllProductsWorker(Pageable pageable);

    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    p.defaultKey AS key,
    col.collection AS collection,
    cat.categoryName AS category
    FROM Product p
    INNER JOIN ProductCollection col ON col.collectionId = p.productCollection.collectionId
    INNER JOIN ProductCategory cat ON cat.categoryId = p.productCategory.categoryId
    INNER JOIN ProductDetail pd ON pd.product.productId = p.productId
    INNER JOIN ProductInventory inv ON inv.productInventoryId = pd.productInventory.productInventoryId
    WHERE pd.isVisible = true AND inv.quantity > 0
    GROUP BY p.uuid, p.name, p.description, p.price, p.currency, p.defaultKey
    """)
    Page<ProductPojo> fetchAllProductsClient(Pageable pageable);

    @Query(value = """
    SELECT
    pd.sku AS sku,
    ps.size AS size,
    pc.colour AS colour,
    GROUP_CONCAT(DISTINCT (img.imageKey)) AS image
    FROM Product p
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductSize ps ON pd.productSize.productSizeId = ps.productSizeId
    INNER JOIN ProductImage img ON pd.productDetailId = img.productDetails.productDetailId
    INNER JOIN ProductColour pc ON pd.productColour.productColourId = pc.productColourId
    INNER JOIN ProductInventory inv ON pd.productInventory.productInventoryId = inv.productInventoryId
    WHERE pd.isVisible = true AND inv.quantity > 0 AND p.name = :name
    GROUP BY ps.size, pc.colour
    """)
    List<DetailPojo> fetchDetailClient(@Param(value = "name") String name);

    @Query(value = "SELECT det FROM ProductDetail det WHERE det.sku = :sku")
    Optional<ProductDetail> findDetailBySku(@Param(value = "sku") String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
    UPDATE Product p
    SET p.name = :name, p.description = :desc, p.price = :price
    WHERE p.uuid = :uuid
    """)
    void updateProduct(
            @Param(value = "uuid") String uuid,
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
    Page<DetailPojo> findDetailByProductNameWorker(@Param(value = "name") String name, Pageable page);

    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    p.defaultKey AS key,
    pc.categoryName AS category
    FROM Product p
    INNER JOIN ProductCategory pc ON p.productCategory.categoryId = pc.categoryId
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductInventory inv ON pd.productInventory.productInventoryId = inv.productInventoryId
    WHERE pd.isVisible = true AND inv.quantity > 0 AND pc.categoryName = :name
    GROUP BY p.uuid, p.name, p.description, p.price, p.currency, p.defaultKey
    """)
    Page<ProductPojo> fetchProductByCategoryClient(String name, Pageable page);

    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    p.defaultKey AS key,
    pc.collection AS collection
    FROM Product p
    INNER JOIN ProductCollection pc ON p.productCollection.collectionId = pc.collectionId
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductInventory inv ON pd.productInventory.productInventoryId = inv.productInventoryId
    WHERE pd.isVisible = true AND inv.quantity > 0 AND pc.collection = :name
    GROUP BY p.uuid, p.name, p.description, p.price, p.currency, p.defaultKey
    """)
    Page<ProductPojo> fetchByProductByCollectionClient(String name, Pageable page);

    @Query(value = """
    SELECT img.imageKey as image
    FROM ProductImage img
    INNER JOIN ProductDetail pd ON img.productDetails.productDetailId = pd.productDetailId
    INNER JOIN Product p ON p.productId = pd.product.productId
    WHERE p.name = :name
    """)
    List<Imagez> images(@Param(value = "name") String name);

}