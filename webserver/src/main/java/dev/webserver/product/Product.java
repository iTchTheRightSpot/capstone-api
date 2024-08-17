package dev.webserver.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "product")
@Builder
public record Product(
        @Id
        @Column("product_id")
        Long productId,
        @NotNull(message = "product uuid cannot be null")
        @NotEmpty(message = "product uuid cannot be empty")
        @Size.List({
                @Size(min = 36, message = "product uuid max length of 36"),
                @Size(max = 36, message = "product uuid max length of 36")
        })
        String uuid,
        @NotNull(message = "product name cannot be null")
        @NotEmpty(message = "product name cannot be empty")
        @Size.List({@Size(max = 50, message = "product name max length of 50")})
        String name,
        @NotNull(message = "product description cannot be null")
        @NotEmpty(message = "product description cannot be empty")
        @Size.List({@Size(max = 2000, message = "product description max length of 2000")})
        String description,
        @NotNull(message = "product default_image_key cannot be null")
        @NotEmpty(message = "product default_image_key cannot be empty")
        @Size.List({
                @Size(min = 36, message = "product default_image_key max length of 36"),
                @Size(max = 36, message = "product default_image_key max length of 36")
        })
        @Column("default_image_key")
        String defaultKey,
        BigDecimal weight,
        @Column("weight_type")
        String weightType, // default injected in migration script
        @NotNull(message = "product category_id cannot be null")
        @Column("category_id")
        Long categoryId
) {}