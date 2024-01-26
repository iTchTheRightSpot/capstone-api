package com.sarabrandserver.product.repository;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.projection.ImagePojo;
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
    Optional<Product> productByName(@Param(value = "name") String name);

    @Query(value = "SELECT p FROM Product p WHERE p.uuid = :uuid")
    Optional<Product> productByUuid(@Param(value = "uuid") String uuid);

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
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS currency,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS price,
    p.defaultKey AS image,
    p.weight AS weight,
    p.weightType AS weightType,
    cat.name AS category
    FROM Product p
    INNER JOIN ProductCategory cat ON p.productCategory.categoryId = cat.categoryId
    """)
    Page<ProductPojo> allProductsAdminFront(SarreCurrency currency, Pageable pageable);

    /**
     * Returns a Product based non default currency
     * */
    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS description,
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS currency,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency ) AS price,
    p.defaultKey AS image,
    p.weight AS weight,
    p.weightType AS weightType,
    cat.name AS category
    FROM Product p
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
    p.productCategory = :category,
    p.weight = :weight
    WHERE p.uuid = :uuid
    """)
    void updateProduct(
            @Param(value = "uuid") String uuid,
            @Param(value = "name") String name,
            @Param(value = "desc") String desc,
            @Param(value = "category") ProductCategory category,
            double weight
    );

    @Query(value = """
    SELECT img.imageKey as image
    FROM ProductImage img
    INNER JOIN ProductDetail pd ON img.productDetails.productDetailId = pd.productDetailId
    INNER JOIN Product p ON p.productId = pd.product.productId
    WHERE p.uuid = :uuid
    """)
    List<ImagePojo> productImagesByProductUUID(@Param(value = "uuid") String uuid);

    @Query("""
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS price,
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS currency,
    p.defaultKey AS image,
    p.weight AS weight,
    p.weightType AS weightType,
    cat.name AS category
    FROM Product p
    INNER JOIN ProductCategory cat ON p.productCategory.categoryId = cat.categoryId
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
    WHERE p.name LIKE :name AND sku.inventory > 0
    GROUP BY p.uuid, p.name, p.defaultKey
    """)
    Page<ProductPojo> productByNameAndCurrency(String name, SarreCurrency currency, Pageable page);

}