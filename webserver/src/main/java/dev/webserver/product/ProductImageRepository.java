package dev.webserver.product;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductImageRepository extends CrudRepository<ProductImage, Long> {

    @Query(value = """
    SELECT i
    FROM ProductImage i
    INNER JOIN ProductDetail d ON i.productDetails.productDetailId = d.productDetailId
    WHERE d.productDetailId = :id
    """)
    List<ProductImage> imagesByProductDetailId(long id);

}
