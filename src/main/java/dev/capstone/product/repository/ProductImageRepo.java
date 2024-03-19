<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/repository/ProductImageRepo.java
package dev.webserver.product.repository;

import dev.webserver.product.entity.ProductImage;
========
package dev.capstone.product.repository;

import dev.capstone.product.entity.ProductImage;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/repository/ProductImageRepo.java
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
    List<ProductImage> imagesByProductDetailId(long id);

}
