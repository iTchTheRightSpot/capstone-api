package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "product_image")
@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ProductImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id", nullable = false, unique = true)
    private Long productImageId;

    @Column(name = "image_path", nullable = false)
    private String ImagePath;

    @ManyToOne
    @JoinColumn(name = "product_detail_id", referencedColumnName = "product_detail_id")
    private ProductDetail productDetail;

    public ProductImage(String imagePath) {
        ImagePath = imagePath;
    }

}
