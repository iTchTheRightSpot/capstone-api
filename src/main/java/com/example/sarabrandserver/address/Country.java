package com.example.sarabrandserver.address;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "country")
@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id", nullable = false, unique = true)
    private Long countryId;

    @Column(name = "country_name", nullable = false)
    private String country;

    @OneToOne(mappedBy = "country")
    private Address address;

    public Country() { }

    public Country(String country) {
        this.country = country;
    }

}
