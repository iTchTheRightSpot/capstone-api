package com.sarabrandserver.address.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "country")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id", nullable = false, unique = true)
    private Long countryId;

    @Column(name = "country_name", nullable = false)
    private String country;

    @OneToOne(mappedBy = "country")
    private Address address;

}
