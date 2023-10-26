package com.sarabrandserver.collection.repository;

import com.sarabrandserver.collection.entity.ProductCollection;
import com.sarabrandserver.collection.projection.CollectionPojo;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.projection.ProductPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface CollectionRepository extends JpaRepository<ProductCollection, Long> {

    @Query(value = "SELECT c FROM ProductCollection c WHERE c.collection = :collection")
    Optional<ProductCollection> findByName(@Param(value = "collection") String collection);

    @Query(value = "SELECT c FROM ProductCollection c WHERE c.uuid = :uuid")
    Optional<ProductCollection> findByUuid(@Param(value = "uuid") String uuid);

    @Query(value = """
            SELECT
            c.uuid AS uuid,
            c.collection AS collection,
            c.createAt AS created,
            c.modifiedAt AS modified,
            c.isVisible AS visible
            FROM ProductCollection c
            """)
    List<CollectionPojo> fetchAllCollection();

    @Query(value = """
            SELECT
            c.collection AS collection,
            c.uuid AS uuid
            FROM ProductCollection c
            WHERE c.isVisible = true
            """)
    List<CollectionPojo> fetchAllCollectionClient();

    @Query(value = """
    SELECT
    COUNT(c.collectionId)
    FROM ProductCollection c
    WHERE c.collection = :name AND c.uuid != :uuid
    """)
    int duplicateCategoryForUpdate(String uuid, String name);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE ProductCollection c
        SET c.modifiedAt = :date, c.collection = :name, c.isVisible = :visible
        WHERE c.uuid = :uuid
    """)
    void update(
            @Param(value = "date") Date date,
            @Param(value = "name") String name,
            @Param(value = "visible") boolean visible,
            @Param(value = "uuid") String id
    );

    @Query(value = """
    SELECT
    p.uuid as uuid,
    p.name as name,
    (SELECT
    c.currency
    FROM PriceCurrency c
    WHERE p.productId = c.product.productId AND c.currency = :currency
    ) AS currency,
    (SELECT
    c.price
    FROM PriceCurrency c
    WHERE p.productId = c.product.productId AND c.currency = :currency
    ) AS price,
    p.defaultKey as key
    FROM Product p
    INNER JOIN ProductCollection c ON p.productCollection.collectionId = c.collectionId
    WHERE c.uuid = :uuid
    """)
    Page<ProductPojo> allProductsByCollection(SarreCurrency currency, String uuid, Pageable page);

}
