package dev.webserver.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "cart")
@Builder
public record Cart(
        @Id
        @Column("cart_id")
        Long cartId,
        int qty,
        @NotNull(message = "cart session_id cannot be null")
        @Column("session_id")
        Long sessionId,
        @NotNull(message = "cart sku_id cannot be null")
        @Column("sku_id")
        Long skuId
) {
}
