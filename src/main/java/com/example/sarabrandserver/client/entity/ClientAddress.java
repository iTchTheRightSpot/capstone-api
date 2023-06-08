package com.example.sarabrandserver.client.entity;

import com.example.sarabrandserver.address.Address;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "client_address")
@Entity
@Getter
@Setter
@EqualsAndHashCode
public class ClientAddress implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_address_id", nullable = false, unique = true)
    private Long clientAddressId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    private Clientz clientz;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id")
    private Address address;

    public ClientAddress() { }

}
