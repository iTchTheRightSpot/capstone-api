package com.sarabrandserver.payment.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "address")
@Entity
@NoArgsConstructor
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

    public Address(
            String address,
            String city,
            String state,
            String postcode,
            String country,
            String deliveryInfo,
            PaymentDetail paymentDetail
    ) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.postcode = postcode;
        this.country = country;
        this.deliveryInfo = deliveryInfo;
        this.paymentDetail = paymentDetail;
    }

    public Long addressId() {
        return addressId;
    }

    public String address() {
        return address;
    }

    public String city() {
        return city;
    }

    public String state() {
        return state;
    }

    public String postcode() {
        return postcode;
    }

    public String country() {
        return country;
    }

    public String deliveryInfo() {
        return deliveryInfo;
    }

    public PaymentDetail paymentDetail() {
        return paymentDetail;
    }

}
