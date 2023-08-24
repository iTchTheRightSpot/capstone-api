package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.projection.DetailPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Long> {

    /**
     * Update a ProductDetail using native MySQL native query as you can update multiple
     * tables in jpql without writing a lot of code
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE product_detail det
            INNER JOIN product_size_inventory psi ON det.size_inventory_id = psi.size_inventory_id
            SET det.modified_at = :d, det.is_visible = :visible, psi.inventory = :qty, psi.size = :s
            WHERE det.sku = :sku
            """,
            nativeQuery = true
    )
    void updateProductDetail(
            @Param(value = "sku") String sku,
            @Param(value = "d") Date date,
            @Param(value = "visible") boolean visible,
            @Param(value = "qty") int qty,
            @Param(value = "s") String size
    );

    @Query(value = """
            SELECT
            pd.sku AS sku,
            pd.colour AS colour,
            ps.size AS size,
            GROUP_CONCAT(DISTINCT (img.imageKey)) AS key
            FROM Product p
            INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
            INNER JOIN ProductSizeInventory ps ON pd.sizeInventory.pairId = ps.pairId
            INNER JOIN ProductImage img ON pd.productDetailId = img.productDetails.productDetailId
            WHERE p.uuid = :uuid
            GROUP BY pd.sku
            """)
    List<DetailPojo> fetchProductDetailByUUIDClient(@Param(value = "uuid") String uuid);

    @Query(value = """
            SELECT COUNT(p.uuid)
            FROM Product p
            INNER JOIN ProductDetail det ON p.productId = det.product.productId
            WHERE p.uuid = :uuid AND det.colour = :colour
            """)
    int colourExist(@Param(value = "uuid") String uuid, @Param(value = "colour") String colour);

    /** For testing purposes. Called in WorkerProductControllerTest. Method updateDetail */
    @Query(value = """
            SELECT det from ProductDetail det
            INNER JOIN FETCH ProductSizeInventory psi ON psi.pairId = det.sizeInventory.pairId
            INNER JOIN FETCH Product p ON p.productId = det.product.productId
            WHERE p.name = :name
            """)
    List<Optional<ProductDetail>> findDetailByName(@Param(value = "name") String name);

}
