package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Table(name = "product_size")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductSize implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_id", nullable = false, unique = true)
    private Long productSizeId;

    @Column(name = "size", nullable = false, length = 100)
    private String size;

    @OneToMany(mappedBy = "productSize")
    private Set<ProductDetail> productDetails;

    public ProductSize(String size) {
        this.size = size;
    }

}
