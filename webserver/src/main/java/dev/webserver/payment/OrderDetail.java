package dev.webserver.payment;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_detail")
@Builder
public record OrderDetail(
        @Id
        @Column("order_detail_id")
        Long orderDetailId,
        int qty,
        @Column("sku_id")
        Long skuId,
        @Column("payment_detail_id")
        Long paymentDetailId
) {
}
