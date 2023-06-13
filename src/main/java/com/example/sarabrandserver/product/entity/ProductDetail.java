package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "product_detail")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id", nullable = false, unique = true)
    private Long productDetailId;

    @Column(name = "description")
    private String description;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "qty", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 25)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedAt;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productDetail", orphanRemoval = true)
    private Set<ProductImage> productImages = new HashSet<>();

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productDetail", orphanRemoval = true)
    private Set<ProductSize> productSizes = new HashSet<>();

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "productDetail", orphanRemoval = true)
    private Set<ProductColour> productColours = new HashSet<>();

    public void addImage(ProductImage image) {
        this.productImages.add(image);
        image.setProductDetail(this);
    }

    public void addSize(ProductSize size) {
        this.productSizes.add(size);
        size.setProductDetail(this);
    }

    public void addColour(ProductColour colour){
        this.productColours.add(colour);
        colour.setProductDetail(this);
    }

    public boolean decrementQuantity() {
        return getQuantity() > 0;
    }

}
