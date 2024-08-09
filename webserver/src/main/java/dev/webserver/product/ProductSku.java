package dev.webserver.product;

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
        String sku,
        String size,
        int inventory,
        @Column("detail_id")
        Long detailId
) {
}
