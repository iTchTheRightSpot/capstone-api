package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "product_item")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_item_id", nullable = false, unique = true)
    private Long productItemId;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "qty_in_stock", nullable = false)
    private int qtyStock;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productItem", orphanRemoval = true)
    private Set<ProductConfiguration> productConfigurations = new HashSet<>();

    public void addProductConfiguration(ProductConfiguration configuration) {
        this.productConfigurations.add(configuration);
        configuration.setProductItem(this);
    }

}
