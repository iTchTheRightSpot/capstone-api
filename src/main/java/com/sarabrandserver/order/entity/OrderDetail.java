package com.sarabrandserver.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import static jakarta.persistence.CascadeType.ALL;

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

    @Column(name = "product_sku", nullable = false, length = 36)
    private String sku; // translates to product_sku table

    @Column(nullable = false)
    private int qty;

    @ManyToOne(cascade = ALL)
    @JoinColumn(name = "payment_detail_id", referencedColumnName = "payment_detail_id")
    private PaymentDetail paymentDetail;

}
