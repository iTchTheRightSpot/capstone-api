package com.emmanuel.sarabrandserver.order.entity;

import com.emmanuel.sarabrandserver.enumeration.GlobalStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "payment_detail")
@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PaymentDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_detail_id", nullable = false, unique = true)
    private Long paymentDetailId;

    // Represents the payment ID from payment provider
    @Column(name = "payment_id", nullable = false, unique = true)
    private String payment_id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_provider", nullable = false, length = 20)
    private String payment_provider;

    @Column(name = "payment_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private GlobalStatus globalStatus;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @OneToOne(mappedBy = "paymentDetail")
    private OrderDetail orderDetail;

}
