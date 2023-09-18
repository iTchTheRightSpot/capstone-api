package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
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

/**
 * All methods ending with worker are for admin dashboard. Client is the opposite
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p WHERE p.name = :name")
    Optional<Product> findByProductName(@Param(value = "name") String name);

    @Query(value = "SELECT p FROM Product p WHERE p.uuid = :uuid")
    Optional<Product> findByProductUuid(@Param(value = "uuid") String uuid);

    @Query(value = """
    SELECT COUNT (p.productId)
    FROM Product p
    WHERE p.name = :name AND p.uuid != :uuid
    """)
    int nameNotAssociatedToUuid(String uuid, String name);

    @Query(value = """
    SELECT COUNT(d.productDetailId)
    FROM ProductDetail d
    INNER JOIN Product p ON d.product.uuid = p.uuid
    WHERE p.uuid = :uuid
    """)
    int productDetailAttach(String uuid);

    @Query(value = """
            SELECT
            p.uuid AS uuid,
            p.name AS name,
            p.description AS desc,
            p.price AS price,
            p.currency AS currency,
            p.defaultKey AS key,
            cat.categoryName AS category,
            col.collection AS collection
            FROM Product p
            INNER JOIN ProductCategory cat ON p.productCategory.categoryId = cat.categoryId
            LEFT JOIN ProductCollection col ON p.productCollection.collectionId = col.collectionId
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
            LEFT JOIN ProductCollection col ON col.collectionId = p.productCollection.collectionId
            INNER JOIN ProductCategory cat ON cat.categoryId = p.productCategory.categoryId
            INNER JOIN ProductDetail pd ON pd.product.productId = p.productId
            INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
            WHERE pd.isVisible = true AND sku.inventory > 0
            GROUP BY p.uuid, p.name, p.description, p.price, p.currency, p.defaultKey
            """)
    Page<ProductPojo> fetchAllProductsClient(Pageable pageable);

    @Query(value = """
            SELECT d FROM ProductDetail d
            INNER JOIN ProductSku s ON d.productDetailId = s.productDetail.productDetailId
            WHERE s.sku = :sku
            """)
    Optional<ProductDetail> findDetailBySku(@Param(value = "sku") String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE Product p
            SET
            p.name = :name,
            p.description = :desc,
            p.price = :price,
            p.productCategory = :category
            WHERE p.uuid = :uuid
            """)
    void updateProductCollectionNotPresent(
            @Param(value = "uuid") String uuid,
            @Param(value = "name") String name,
            @Param(value = "desc") String desc,
            @Param(value = "price") BigDecimal price,
            @Param(value = "category") ProductCategory category
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE Product p
            SET
            p.name = :name,
            p.description = :desc,
            p.price = :price,
            p.productCategory = :category,
            p.productCollection = :collection
            WHERE p.uuid = :uuid
            """)
    void updateProductCategoryCollectionPresent(
            @Param(value = "uuid") String uuid,
            @Param(value = "name") String name,
            @Param(value = "desc") String desc,
            @Param(value = "price") BigDecimal price,
            @Param(value = "category") ProductCategory category,
            @Param(value = "collection") ProductCollection collection
    );

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
            INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
            WHERE pd.isVisible = true AND sku.inventory > 0 AND pc.categoryName = :name
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
            INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
            WHERE pd.isVisible = true AND sku.inventory > 0 AND pc.collection = :name
            GROUP BY p.uuid, p.name, p.description, p.price, p.currency, p.defaultKey
            """)
    Page<ProductPojo> fetchByProductByCollectionClient(String name, Pageable page);

    @Query(value = """
            SELECT img.imageKey as image
            FROM ProductImage img
            INNER JOIN ProductDetail pd ON img.productDetails.productDetailId = pd.productDetailId
            INNER JOIN Product p ON p.productId = pd.product.productId
            WHERE p.uuid = :uuid
            """)
    List<Imagez> images(@Param(value = "uuid") String uuid);

}