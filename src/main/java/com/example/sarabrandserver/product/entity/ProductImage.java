package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Table(name = "product_image")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ProductImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id", nullable = false, unique = true)
    private Long productImageId;

    @Column(name = "image_key", nullable = false, unique = true, updatable = false, length = 50)
    private String imageKey; // Represents key in AWS or DigitalOcean bucket

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "product_detail_id", referencedColumnName = "product_detail_id")
    private ProductDetail productDetail;

    public ProductImage(String imageKey, String imagePath) {
        this.imageKey = imageKey;
        this.imagePath = imagePath;
    }

}
