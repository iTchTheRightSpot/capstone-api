package dev.webserver.product;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "product_image")
@Builder
public record ProductImage(
        @Id
        @Column("image_id")
        Long productImageId,
        @Column("image_key")
        String imageKey, // represents key in AWS or DigitalOcean bucket
        @Column("image_path")
        String imagePath,
        @Column("detail_id")
        Long productDetailId
) {
}
