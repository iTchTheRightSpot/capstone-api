package com.example.sarabrandserver.product.category.entity;

import com.example.sarabrandserver.product.entity.Product;
import com.example.sarabrandserver.product.variation.Variation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "product_category")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, unique = true)
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "parent_category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productCategory", orphanRemoval = true)
    private Set<ProductCategory> productCategories = new HashSet<>();

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productCategory", orphanRemoval = true)
    private Set<Product> product = new HashSet<>();

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productCategory", orphanRemoval = true)
    private Set<Variation> variations = new HashSet<>();

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

    public void addVariation(Variation variation) {
        this.variations.add(variation);
        variation.setProductCategory(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCategory that)) return false;
        return Objects.equals(getCategoryId(), that.getCategoryId())
                && Objects.equals(getCategoryName(), that.getCategoryName())
                && Objects.equals(getProductCategory(), that.getProductCategory())
                && Objects.equals(getProductCategories(), that.getProductCategories())
                && Objects.equals(getProduct(), that.getProduct())
                && Objects.equals(getVariations(), that.getVariations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCategoryId(),
                getCategoryName(),
                getProductCategory(),
                getProductCategories(),
                getProduct(),
                getVariations());
    }
}
