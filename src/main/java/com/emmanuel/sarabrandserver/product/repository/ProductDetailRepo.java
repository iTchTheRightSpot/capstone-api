package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.projection.DetailPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Long> {

    /**
     * Update a ProductDetail and ProductSku using native MySQL query as you can update multiple
     * tables in jpql without writing a lot of code
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE product_sku s
            INNER JOIN product_detail d ON d.detail_id = s.detail_id
            SET d.is_visible = :visible, s.inventory = :qty, s.size = :s
            WHERE s.sku = :sku
            """,
            nativeQuery = true
    )
    void updateProductDetail(
            @Param(value = "sku") String sku,
            @Param(value = "visible") boolean visible,
            @Param(value = "qty") int qty,
            @Param(value = "s") String size
    );

    @Query(value = """
            SELECT
            d.colour AS colour,
            d.isVisible AS visible,
            img AS image,
            d.skus as skus
            FROM ProductDetail d
            INNER JOIN Product p ON d.product.productId = p.productId
            INNER JOIN ProductImage img ON d.productDetailId = img.productDetails.productDetailId
            WHERE d.isVisible AND p.uuid = :uuid
            """)
    List<DetailPojo> fetchProductDetailByUUIDClient(@Param(value = "uuid") String uuid);

    @Query(value = """
            SELECT COUNT(p.uuid)
            FROM Product p
            INNER JOIN ProductDetail det ON p.productId = det.product.productId
            WHERE p.uuid = :uuid AND det.colour = :colour
            """)
    int colourExist(@Param(value = "uuid") String uuid, @Param(value = "colour") String colour);

    @Query(value = """
            SELECT
            d.colour AS colour,
            d.isVisible AS visible,
            img AS image,
            d.skus as skus
            FROM ProductDetail d
            INNER JOIN Product p ON d.product.productId = p.productId
            INNER JOIN ProductImage img ON d.productDetailId = img.productDetails.productDetailId
            WHERE p.uuid = :uuid
            """)
    List<DetailPojo> findProductDetailsByProductUuidWorker(@Param(value = "uuid") String uuid);

}
