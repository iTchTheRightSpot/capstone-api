package dev.webserver.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_detail")
@Builder
public record OrderDetail(
        @Id
        @Column("order_id")
        Long orderId,
        int qty,
        @NotNull(message = "order_detail sku_id cannot be null")
        @Column("sku_id")
        Long skuId,
        @NotNull(message = "order_detail payment_id cannot be null")
        @Column("payment_id")
        Long paymentDetailId
) {
}
