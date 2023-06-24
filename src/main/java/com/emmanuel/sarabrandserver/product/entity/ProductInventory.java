package com.emmanuel.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Table(name = "product_inventory")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id", nullable = false, unique = true)
    private Long productInventoryId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @OneToMany(mappedBy = "productInventory")
    private Set<ProductDetail> productDetails;

    public ProductInventory(int quantity) {
        this.quantity = quantity;
    }

}
