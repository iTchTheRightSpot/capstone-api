package dev.webserver.product;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "product_detail")
@Builder
public record ProductDetail(
        @Id
        @Column("detail_id")
        Long detailId,
        String colour,
        @Column("is_visible")
        boolean isVisible,
        @Column("created_at")
        LocalDateTime createAt,
        @Column("product_id")
        Long productId
) {
}
