package com.emmanuel.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product_detail")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id", nullable = false, unique = true)
    private Long productDetailId;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "is_visible")
    private boolean isVisible;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @ManyToOne(fetch = EAGER, cascade = ALL)
    @JoinColumn(name = "size_id", referencedColumnName = "size_id", nullable = false)
    private ProductSize productSize;

    @ManyToOne(fetch = EAGER, cascade = ALL)
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id", nullable = false)
    private ProductInventory productInventory;

    @ManyToOne(fetch = EAGER, cascade = ALL)
    @JoinColumn(name = "colour_id", referencedColumnName = "colour_id", nullable = false)
    private ProductColour productColour;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

    @OneToMany(fetch = EAGER, cascade = ALL, mappedBy = "productDetails", orphanRemoval = true)
    private Set<ProductImage> productImages;

    public void setProductSize(ProductSize productSize) {
        this.productSize = productSize;
        this.productSize.getProductDetails().add(this);
    }

    public void setProductInventory(ProductInventory productInventory) {
        this.productInventory = productInventory;
        this.productInventory.getProductDetails().add(this);
    }

    public void addImages(ProductImage productImage) {
        this.productImages.add(productImage);
        productImage.setProductDetails(this);
    }

    public void setProductColour(ProductColour productColour) {
        this.productColour = productColour;
        this.productColour.getProductDetails().add(this);
    }

}
