package com.emmanuel.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Table(name = "product_size_inventory")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductSizeInventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_inventory_id", nullable = false, unique = true, updatable = false)
    private Long pairId;

    @Column(name = "size", nullable = false, length = 100)
    private String size;

    @Column(name = "inventory", nullable = false)
    private int inventory;

    @OneToMany(mappedBy = "sizeInventory")
    private Set<ProductDetail> productDetails;

    public ProductSizeInventory(String size, int inventory) {
        this.size = size;
        this.inventory = inventory;
    }

}
