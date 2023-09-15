package com.emmanuel.sarabrandserver.collection.entity;

import com.emmanuel.sarabrandserver.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Class replicates the collection or season product replicates.
 * Look for better understanding <a href="https://www.samawoman.com/">...</a>
 * */
@Table(name = "product_collection", indexes = @Index(name = "IX_product_collection_uuid", columnList = "uuid"))
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id", nullable = false, unique = true)
    private Long collectionId;

    @Column(name = "uuid", length = 36, nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "collection", nullable = false, unique = true, length = 50)
    private String collection;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @Column(name = "is_visible")
    private boolean isVisible;

    @OneToMany(cascade = CascadeType.ALL, fetch = LAZY, mappedBy = "productCollection")
    private Set<Product> products;

}
