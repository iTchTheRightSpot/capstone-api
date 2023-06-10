package com.example.sarabrandserver.category.entity;

import com.example.sarabrandserver.product.entity.Product;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "product_category")
@Entity
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ProductCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, unique = true)
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true, length = 32)
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
    private Set<ProductCategory> productCategories = new HashSet<>();

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productCategory", orphanRemoval = true)
    private Set<Product> product = new HashSet<>();

    public ProductCategory(String categoryName) {
        this.categoryName = categoryName;
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
