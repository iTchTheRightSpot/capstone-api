package dev.webserver.product;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductImageRepository extends CrudRepository<ProductImage, Long> {

    @Query(value = """
    SELECT
        i.*
    FROM product_image i
    INNER JOIN product_detail d ON i.detail_id = d.detail_id
    WHERE d.detail_id = :id
    """)
    List<ProductImage> imagesByProductDetailId(long id);

}
