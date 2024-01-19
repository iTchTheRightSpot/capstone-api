package com.sarabrandserver.shipping;

import com.sarabrandserver.enumeration.ShippingType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Table(name = "shipping")
@Entity
@NoArgsConstructor
@Setter
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_id", nullable = false, unique = true)
    private Long shippingId;

    @Column(name = "ngn_price", nullable = false)
    private BigDecimal ngnPrice;

    @Column(name = "usd_price", nullable = false)
    private BigDecimal usdPrice;

    @Column(name = "shipping_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ShippingType shippingType;

    public Shipping(BigDecimal ngnPrice, BigDecimal usdPrice, ShippingType shippingType) {
        this.ngnPrice = ngnPrice;
        this.usdPrice = usdPrice;
        this.shippingType = shippingType;
    }

    public Long shippingId() {
        return shippingId;
    }

    public BigDecimal ngnPrice() {
        return ngnPrice;
    }

    public BigDecimal usdPrice() {
        return usdPrice;
    }

    public ShippingType type() {
        return shippingType;
    }

}
