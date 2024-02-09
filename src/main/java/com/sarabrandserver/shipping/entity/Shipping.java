package com.sarabrandserver.shipping.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A representation of countries we are allowed to ship to.
 * */
@Table(name = "shipping")
@Entity
@NoArgsConstructor
@Setter
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_id", nullable = false, unique = true)
    private Long shippingId;

    @Column(nullable = false, unique = true)
    private String country;

    @Column(name = "ngn_price", nullable = false)
    private BigDecimal ngnPrice;

    @Column(name = "usd_price", nullable = false)
    private BigDecimal usdPrice;

    public Shipping(String country, BigDecimal ngnPrice, BigDecimal usdPrice) {
        this.country = country;
        this.ngnPrice = ngnPrice;
        this.usdPrice = usdPrice;
    }

    public Long shippingId() {
        return shippingId;
    }

    public String country() { return country; }

    public BigDecimal ngnPrice() {
        return ngnPrice;
    }

    public BigDecimal usdPrice() {
        return usdPrice;
    }

}
