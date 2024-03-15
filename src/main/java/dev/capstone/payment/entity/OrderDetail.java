package dev.capstone.payment.entity;

import dev.capstone.product.entity.ProductSku;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "order_detail")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class OrderDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id", nullable = false, unique = true)
    private Long orderDetailId;

    @Column(nullable = false)
    private int qty;

    @ManyToOne
    @JoinColumn(name = "sku_id", referencedColumnName = "sku_id", nullable = false)
    private ProductSku sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_detail_id", referencedColumnName = "payment_detail_id", nullable = false)
    private PaymentDetail paymentDetail;

    public OrderDetail(int qty, ProductSku sku, PaymentDetail detail) {
        this.qty = qty;
        this.sku = sku;
        this.paymentDetail = detail;
    }

}
