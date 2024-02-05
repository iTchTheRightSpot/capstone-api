package com.sarabrandserver.payment.entity;

import com.sarabrandserver.enumeration.PaymentStatus;
import com.sarabrandserver.enumeration.SarreCurrency;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "payment_detail")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_detail_id", nullable = false, unique = true)
    private Long paymentDetailId;

    @Column(name = "full_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SarreCurrency currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_provider", nullable = false, length = 30)
    private String paymentProvider;

    @Column(name = "payment_status", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @OneToOne(mappedBy = "paymentDetail", cascade = { PERSIST, MERGE, REFRESH })
    @PrimaryKeyJoinColumn
    private Address address;

    @OneToMany(fetch = LAZY, cascade = { PERSIST, MERGE, REFRESH }, mappedBy = "paymentDetail")
    private Set<OrderDetail> orderDetails;

}
