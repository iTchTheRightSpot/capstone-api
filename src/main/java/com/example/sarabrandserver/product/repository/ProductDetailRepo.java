package com.example.sarabrandserver.product.repository;

import com.example.sarabrandserver.product.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE ProductDetail p
    SET p.deletedAt = :date, p.modifiedAt = :date
    WHERE p.productDetailId = :id
    AND p.sku = :sku
    """)
    void custom_delete(
            @Param(value = "date") Date date,
            @Param(value = "id") long id,
            @Param(value = "sku") String sku
    );

    /**
     * Query method finds a ProductDetail by its Product ID and sku
     * @param id is Product ID
     * @param sku is of ProductDetail stock keeping unit (unique to each product)
     * @return Optional of type ProductDetail
     * */
    @Query(value = """
    SELECT det FROM ProductDetail det
    INNER JOIN Product p
    ON det.product.productId = p.productId
    WHERE p.productId = :id AND det.sku = :sku
    """)
    Optional<ProductDetail> findByProductDetail(@Param(value = "id") long id, @Param(value = "sku") String sku);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE ProductImage AS pm
    SET pm.imagePath = :path
    WHERE pm.productDetail.productDetailId IN (
        SELECT det
        FROM ProductDetail det
        WHERE det.productDetailId = :id
    )
    """)
    void updateImage(@Param(value = "id") long id, @Param(value = "path") String path);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE ProductDetail det
    SET det.description = :description, det.price = :price, det.quantity = :qty, det.modifiedAt = :date
    WHERE det.productDetailId = :id
    """)
    void updateProductDetail(
            @Param(value = "id") long id,
            @Param(value = "desc") String desc,
            @Param(value = "price") BigDecimal price,
            @Param(value = "qty") int qty,
            @Param(value = "date") Date date
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE ProductSize AS ps
    SET ps.size = :size
    WHERE ps.productDetail.productDetailId IN (
        SELECT det
        FROM ProductDetail det
        WHERE det.productDetailId = :id
    )
    """)
    void updateProductDetailSize(@Param(value = "id") long id, @Param(value = "size") String size);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE ProductColour AS pc
    SET pc.colour = :colour
    WHERE pc.productDetail.productDetailId IN (
        SELECT det
        FROM ProductDetail det
        WHERE det.productDetailId = :id
    )
    """)
    void updateProductDetailColour(@Param(value = "id") long id, @Param(value = "colour") String colour);

}
