package dev.webserver.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "product_sku")
@Builder
public record ProductSku(
        @Id
        @Column("sku_id")
        Long skuId,
        @NotNull(message = "product_sku uuid cannot be null")
        @NotEmpty(message = "product_sku uuid cannot be empty")
        @Size.List({
                @Size(min = 36, message = "product_sku uuid max length of 36"),
                @Size(max = 36, message = "product_sku uuid max length of 36")
        })
        String sku,
        @NotNull(message = "product_sku size cannot be null")
        @NotEmpty(message = "product_sku size cannot be empty")
        @Size.List({
                @Size(min = 50, message = "product_sku size max length of 50"),
                @Size(max = 50, message = "product_sku size max length of 50")
        })
        String size,
        int inventory,
        @NotNull(message = "product_sku detail_id cannot be null")
        @Column("detail_id")
        Long detailId
) {
}
