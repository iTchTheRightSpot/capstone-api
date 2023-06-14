package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

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
    @Column(name = "product_size_id", nullable = false, unique = true)
    private Long productSizeId;

    @Column(name = "size", nullable = false)
    private String size;

    @ManyToOne
    @JoinColumn(name = "product_detail_id", referencedColumnName = "product_detail_id")
    private ProductDetail productDetail;

    public ProductSize(String size) {
        this.size = size;
    }

}
