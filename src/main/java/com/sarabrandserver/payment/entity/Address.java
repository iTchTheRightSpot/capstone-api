package com.sarabrandserver.payment.entity;

import com.sarabrandserver.payment.entity.PaymentDetail;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Table(name = "address")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Address implements Serializable {

    @Id
    @Column(name = "address_id", nullable = false, unique = true)
    private Long addressId;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "postcode", length = 10)
    private String postcode;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "delivery_info", nullable = false, length = 1000)
    private String deliveryInfo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "address_id")
    private PaymentDetail paymentDetail;

}
