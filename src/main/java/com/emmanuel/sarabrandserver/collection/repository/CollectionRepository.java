package com.emmanuel.sarabrandserver.collection.repository;

import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.projection.CollectionPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<ProductCollection, Long> {

    @Query(value = "SELECT c FROM ProductCollection c WHERE c.collection = :collection")
    Optional<ProductCollection> findByName(@Param(value = "collection") String collection);

    @Query(value = """
            SELECT
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
            c.createAt AS created,
            c.modifiedAt AS modified,
            c.isVisible AS visible
            FROM ProductCollection c
            WHERE c.isVisible = true
            """)
    List<CollectionPojo> fetchAllCollectionClient();

}
