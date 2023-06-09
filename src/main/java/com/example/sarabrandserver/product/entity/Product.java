package com.example.sarabrandserver.product.entity;

import com.example.sarabrandserver.product.entity.category.entity.ProductCategory;
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

@Table(name = "product")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "product", orphanRemoval = true)
    private Set<ProductItem> productItems = new HashSet<>();

    public void addProductItem(ProductItem item) {
        this.productItems.add(item);
        item.setProduct(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(getProductId(), product.getProductId())
                && Objects.equals(getName(), product.getName())
                && Objects.equals(getDescription(), product.getDescription())
                && Objects.equals(getImagePath(), product.getImagePath())
                && Objects.equals(getProductCategory(), product.getProductCategory())
                && Objects.equals(getProductItems(), product.getProductItems());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductId(),
                getName(),
                getDescription(),
                getImagePath(),
                getProductCategory(),
                getProductItems());
    }
}
