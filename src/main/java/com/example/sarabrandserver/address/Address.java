package com.example.sarabrandserver.address;

import com.example.sarabrandserver.client.entity.ClientAddress;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "address")
@Entity
@Getter
@Setter
@EqualsAndHashCode
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

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "address", orphanRemoval = true)
    private Set<ClientAddress> clientAddress;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id", nullable = false)
    private Country country;

    public Address() { }

    public void addClientAddress(ClientAddress address) {
        this.clientAddress.add(address);
        address.setAddress(this);
    }

    public void setCountry(Country country) {
        this.country = country;
        country.setAddress(this);
    }

}
