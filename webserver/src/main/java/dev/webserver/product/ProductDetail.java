package dev.webserver.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "product_detail")
@Builder
public record ProductDetail(
        @Id
        @Column("detail_id")
        Long detailId,
        @NotNull(message = "product_detail colour cannot be null")
        @NotEmpty(message = "product_detail colour cannot be empty")
        @Size.List({@Size(max = 50, message = "product_detail colour max length of 50")})
        String colour,
        @Column("is_visible")
        boolean isVisible,
        @NotNull(message = "product_detail product_id cannot be null")
        @Column("product_id")
        Long productId
) {
}
