package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Long> {

    /**
     * Update a ProductDetail using native MySQL native query as you can update multiple
     * tables in jpql without writing a lot of code
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
    UPDATE product_detail det
    INNER JOIN product_inventory pi ON det.inventory_id = pi.inventory_id
    INNER JOIN product_size ps ON det.size_id = ps.size_id
    SET det.modified_at = :d, det.is_visible = :visible, pi.quantity = :qty, ps.size = :s
    WHERE det.sku = :sku
    """, nativeQuery = true)
    void updateProductDetail(
            @Param(value = "sku") String sku,
            @Param(value = "d") Date date,
            @Param(value = "visible") boolean visible,
            @Param(value = "qty") int qty,
            @Param(value = "s") String size
    );

}
