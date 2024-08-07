package dev.webserver.cart;

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
        @Column("session_id")
        Long sessionId,
        @Column("sku_id")
        Long skuId
) {
}
