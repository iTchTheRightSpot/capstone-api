package dev.webserver.shipping;

import jakarta.persistence.*;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A representation of countries we are allowed to ship to.
 * */
@Table(name = "ship_setting")
@Entity
@Setter
public class ShipSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ship_id", nullable = false, unique = true)
    private Long shipId;

    @Column(nullable = false, unique = true)
    private String country;

    @Column(name = "ngn_price", nullable = false)
    private BigDecimal ngnPrice;

    @Column(name = "usd_price", nullable = false)
    private BigDecimal usdPrice;

    public ShipSetting() {
    }

    public ShipSetting(String country, BigDecimal ngnPrice, BigDecimal usdPrice) {
        this.country = country;
        this.ngnPrice = ngnPrice;
        this.usdPrice = usdPrice;
    }

    public Long shipId() {
        return shipId;
    }

    public String country() {
        return country;
    }

    public BigDecimal ngnPrice() {
        return ngnPrice;
    }

    public BigDecimal usdPrice() {
        return usdPrice;
    }

}
