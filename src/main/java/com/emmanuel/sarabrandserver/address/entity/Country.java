package com.emmanuel.sarabrandserver.address.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "country")
@Entity
@NoArgsConstructor
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

    public Country(String country) {
        this.country = country;
    }

}
