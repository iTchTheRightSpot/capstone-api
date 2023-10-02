package com.sarabrandserver.product.repository;

import com.sarabrandserver.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepo extends JpaRepository<ProductImage, Long> {

    @Query(value = """
    SELECT i
    FROM ProductImage i
    INNER JOIN ProductDetail d ON i.productDetails.productDetailId = d.productDetailId
    WHERE d.productDetailId = :id
    """)
    List<ProductImage> imagesByProductDetailID(long id);

}
