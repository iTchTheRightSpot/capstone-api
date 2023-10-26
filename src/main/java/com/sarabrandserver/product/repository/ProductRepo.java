package com.sarabrandserver.product.repository;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.collection.entity.ProductCollection;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.projection.Imagez;
import com.sarabrandserver.product.projection.ProductPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

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
            p.description AS description,
            (SELECT
            c.currency
            FROM PriceCurrency c
            WHERE p.productId = c.product.productId AND c.currency = :currency
            ) AS currency,
            (SELECT
            c.price
            FROM PriceCurrency c
            WHERE p.productId = c.product.productId AND c.currency = :currency
            ) AS price,
            p.defaultKey AS key,
            cat.categoryName AS category,
            col.collection AS collection
            FROM Product p
            INNER JOIN ProductCategory cat ON p.productCategory.categoryId = cat.categoryId
            LEFT JOIN ProductCollection col ON p.productCollection.collectionId = col.collectionId
            """)
    Page<ProductPojo> fetchAllProductsWorker(SarreCurrency currency, Pageable pageable);

    /** Returns a Product based non default currency */
    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS description,
    (SELECT
    c.currency
    FROM PriceCurrency c
    WHERE p.productId = c.product.productId AND c.currency = :currency
    ) AS currency,
    (SELECT
    c.price
    FROM PriceCurrency c
    WHERE p.productId = c.product.productId AND c.currency = :currency
    ) AS price,
    p.defaultKey AS key,
    col.collection AS collection,
    cat.categoryName AS category
    FROM Product p
    LEFT JOIN ProductCollection col ON col.collectionId = p.productCollection.collectionId
    INNER JOIN ProductCategory cat ON cat.categoryId = p.productCategory.categoryId
    INNER JOIN ProductDetail pd ON pd.product.productId = p.productId
    INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
    WHERE cat.isVisible = TRUE AND pd.isVisible = TRUE AND sku.inventory > 0
    GROUP BY p.uuid, p.name, p.description, p.defaultKey
    """)
    Page<ProductPojo> allProductsByCurrencyClient(SarreCurrency currency, Pageable pageable);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE Product p
            SET
            p.name = :name,
            p.description = :desc,
            p.productCategory = :category
            WHERE p.uuid = :uuid
            """)
    void update_product_where_collection_not_present(
            @Param(value = "uuid") String uuid,
            @Param(value = "name") String name,
            @Param(value = "desc") String desc,
            @Param(value = "category") ProductCategory category
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE Product p
            SET
            p.name = :name,
            p.description = :desc,
            p.productCategory = :category,
            p.productCollection = :collection
            WHERE p.uuid = :uuid
            """)
    void update_product_where_category_and_collection_are_present(
            @Param(value = "uuid") String uuid,
            @Param(value = "name") String name,
            @Param(value = "desc") String desc,
            @Param(value = "category") ProductCategory category,
            @Param(value = "collection") ProductCollection collection
    );

    @Query(value = """
            SELECT
            p.uuid AS uuid,
            p.name AS name,
            p.description AS description,
            (SELECT
            c.currency
            FROM PriceCurrency c
            WHERE p.productId = c.product.productId AND c.currency = :currency
            ) AS currency,
            (SELECT
            c.price
            FROM PriceCurrency c
            WHERE p.productId = c.product.productId AND c.currency = :currency
            ) AS price,
            p.defaultKey AS key,
            pc.categoryName AS category
            FROM Product p
            INNER JOIN ProductCategory pc ON p.productCategory.categoryId = pc.categoryId
            INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
            INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
            WHERE pd.isVisible = true AND sku.inventory > 0 AND pc.uuid = :uuid
            GROUP BY p.uuid, p.name, p.description, p.defaultKey
            """)
    Page<ProductPojo> fetchProductByCategoryClient(SarreCurrency currency, String uuid, Pageable page);

    @Query(value = """
            SELECT
            p.uuid AS uuid,
            p.name AS name,
            p.description AS description,
            (SELECT
            c.currency
            FROM PriceCurrency c
            WHERE p.productId = c.product.productId AND c.currency = :currency
            ) AS currency,
            (SELECT
            c.price
            FROM PriceCurrency c
            WHERE p.productId = c.product.productId AND c.currency = :currency
            ) AS price,
            p.defaultKey AS key,
            pc.collection AS collection
            FROM Product p
            INNER JOIN ProductCollection pc ON p.productCollection.collectionId = pc.collectionId
            INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
            INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
            WHERE pd.isVisible = true AND sku.inventory > 0 AND pc.uuid = :uuid
            GROUP BY p.uuid, p.name, p.description, p.defaultKey
            """)
    Page<ProductPojo> fetchByProductByCollectionClient(SarreCurrency currency, String uuid, Pageable page);

    @Query(value = """
            SELECT img.imageKey as image
            FROM ProductImage img
            INNER JOIN ProductDetail pd ON img.productDetails.productDetailId = pd.productDetailId
            INNER JOIN Product p ON p.productId = pd.product.productId
            WHERE p.uuid = :uuid
            """)
    List<Imagez> productImagesByProductUUID(@Param(value = "uuid") String uuid);

}