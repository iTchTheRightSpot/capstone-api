package dev.webserver.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "product_image")
@Builder
public record ProductImage(
        @Id
        @Column("image_id")
        Long imageId,
        @NotNull(message = "product_image uuid cannot be null")
        @NotEmpty(message = "product_image uuid cannot be empty")
        @Size.List({
                @Size(min = 36, message = "product_image uuid max length of 36"),
                @Size(max = 36, message = "product_image uuid max length of 36")
        })
        @Column("image_key")
        String imageKey, // represents key in AWS or DigitalOcean bucket
        @NotNull(message = "product_image detail_id cannot be null")
        @Column("detail_id")
        Long detailId
) {
}
