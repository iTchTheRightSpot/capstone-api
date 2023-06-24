package com.emmanuel.sarabrandserver.order.entity;

import com.emmanuel.sarabrandserver.address.entity.Address;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static jakarta.persistence.CascadeType.ALL;

@Table(name = "order_detail")
@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrderDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id", nullable = false, unique = true)
    private Long orderDetailId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @OneToOne(mappedBy = "orderDetail")
    private OrderItem orderItem;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "payment_detail_id", referencedColumnName = "payment_detail_id", nullable = false)
    private PaymentDetail paymentDetail;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id", nullable = false)
    private Address address;

}
