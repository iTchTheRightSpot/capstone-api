package com.example.sarabrandserver.product.repository;

import com.example.sarabrandserver.product.entity.Product;
import com.example.sarabrandserver.product.projection.ProductPojo;
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
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p WHERE p.name = :name")
    Optional<Product> findByProductName(@Param(value = "name") String name);

    @Query(value = """
    SELECT p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    pd.sku AS sku,
    pd.isDisabled AS status,
    ps.size AS sizes,
    inv.quantity AS quantity,
    img.imageKey AS image,
    pc.colour AS colour
    FROM Product p
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductSize ps ON pd.productSize.productSizeId = ps.productSizeId
    INNER JOIN ProductInventory inv ON pd.productInventory.productInventoryId = inv.productInventoryId
    INNER JOIN ProductImage img ON pd.productImage.productImageId = img.productImageId
    INNER JOIN ProductColour pc ON pd.productColour.productColourId = pc.productColourId
    """)
    List<ProductPojo> fetchAll(Pageable pageable);

    /**
     * Query method finds a ProductDetail by its Product name and ProductDetail sku
     * @param name is Product name
     * @param sku is of ProductDetail
     * @return Optional of type ProductDetail
     * */
    @Query(value = """
    SELECT p FROM Product p
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    WHERE p.name = :name AND pd.sku = :sku
    """)
    Optional<Product> findByProductNameAndSku(@Param(value = "name") String name, @Param(value = "sku") String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Product p WHERE p.name = :name")
    void custom_delete(@Param(value = "name") String name);

}
