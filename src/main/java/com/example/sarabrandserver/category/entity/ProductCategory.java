package com.example.sarabrandserver.category.entity;

import com.example.sarabrandserver.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product_category")
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

    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedAt;

    @ManyToOne
    @JoinColumn(name = "parent_category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productCategory", orphanRemoval = true)
    private Set<ProductCategory> productCategories;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "productCategory", orphanRemoval = true)
    private Set<Product> product;

    public ProductCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public ProductCategory(String categoryName, Date createAt) {
        this.categoryName = categoryName;
        this.createAt = createAt;
    }

    public void addCategory(ProductCategory category) {
        this.productCategories.add(category);
        category.setProductCategory(this);
    }

    public void addProduct(Product product) {
        this.product.add(product);
        product.setProductCategory(this);
    }

}
