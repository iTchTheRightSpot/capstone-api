package com.sarabrandserver.address.entity;

import com.sarabrandserver.order.entity.OrderDetail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import static jakarta.persistence.CascadeType.ALL;

@Table(name = "address")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Address implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false, unique = true)
    private Long addressId;

    @Column(name = "unit_number")
    private long unitNumber;

    @Column(name = "street_number")
    private long street_number;

    @Column(name = "address_line1")
    private String address1;

    @Column(name = "address_line2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "region")
    private String region;

    @Column(name = "postal_code")
    private String postalCode;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id", nullable = false)
    private Country country;

    @OneToOne(mappedBy = "address")
    private OrderDetail orderDetail;

    public void setCountry(Country country) {
        this.country = country;
        country.setAddress(this);
    }

    public void setOrderDetail(OrderDetail detail) {
        this.orderDetail = detail;
        detail.setAddress(this);
    }

}
