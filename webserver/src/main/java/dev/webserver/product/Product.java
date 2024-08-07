package dev.webserver.product;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "product")
@Builder
public record Product(
        @Id
        @Column("product_id")
        Long productId,
        String uuid,
        String name,
        String description,
        @Column("default_image_key")
        String defaultKey,
        Double weight,
        @Column("weight_type")
        String weightType, // default injected in migration script
        @Column("category_id")
        Long categoryId
) {}