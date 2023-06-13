package com.example.sarabrandserver.product.repository;

import com.example.sarabrandserver.product.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
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
}
