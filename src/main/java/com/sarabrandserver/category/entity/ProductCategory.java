package com.sarabrandserver.category.entity;

import com.sarabrandserver.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product_category", indexes = @Index(name = "IX_product_category_uuid", columnList = "uuid"))
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, unique = true)
    private Long categoryId;

    @Column(name = "uuid", length = 36, nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @Column(name = "is_visible")
    private boolean isVisible;

    @ManyToOne
    @JoinColumn(name = "parent_category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "productCategory", orphanRemoval = true)
    private Set<ProductCategory> productCategories;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "productCategory")
    private Set<Product> product;

    public void addCategory(ProductCategory category) {
        this.productCategories.add(category);
        category.setProductCategory(this);
    }

}
