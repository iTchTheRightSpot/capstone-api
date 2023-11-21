package com.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product_sku", indexes = @Index(name = "IX_product_sku_sku", columnList = "sku"))
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductSku implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id", nullable = false, unique = true)
    private Long skuId;

    @Column(name = "sku", nullable = false, unique = true, length = 36)
    private String sku;

    @Column(name = "size", nullable = false, length = 50)
    private String size;

    @Column(name = "inventory", nullable = false)
    private int inventory;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "detail_id", referencedColumnName = "detail_id", nullable = false)
    private ProductDetail productDetail;

    public ProductSku(String sku, String size, int inventory, ProductDetail productDetail) {
        this.sku = sku;
        this.size = size;
        this.inventory = inventory;
        this.productDetail = productDetail;
    }

}
