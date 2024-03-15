package dev.capstone.product.entity;

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
public class ProductImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false, unique = true)
    private Long productImageId;

    @Column(name = "image_key", nullable = false, unique = true, updatable = false, length = 50)
    private String imageKey; // represents key in AWS or DigitalOcean bucket

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", referencedColumnName = "detail_id", nullable = false)
    private ProductDetail productDetails;

    public ProductImage(String imageKey, String imagePath, ProductDetail detail) {
        this.imageKey = imageKey;
        this.imagePath = imagePath;
        this.productDetails = detail;
    }

}
