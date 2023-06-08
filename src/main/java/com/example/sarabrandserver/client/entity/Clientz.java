package com.example.sarabrandserver.client.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "clientz")
@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Clientz implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id", nullable = false, unique = true)
    private Long clientId;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "credentials_none_expired", nullable = false)
    private boolean credentialsNonExpired;

    @Column(name = "account_none_expired", nullable = false)
    private boolean accountNonExpired;

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @OneToMany(cascade = {PERSIST, MERGE, REMOVE}, fetch = EAGER, mappedBy = "clientz", orphanRemoval = true)
    private Set<ClientRole> clientRole = new HashSet<>();

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "clientz", orphanRemoval = true)
    private Set<ClientAddress> clientAddress = new HashSet<>();

    public Clientz() {}

    public Clientz(
            String firstname,
            String lastname,
            String email,
            String phoneNumber,
            String password,
            boolean enabled,
            boolean credentialsNonExpired,
            boolean accountNonExpired,
            boolean locked
    ) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.enabled = enabled;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonExpired = accountNonExpired;
        this.locked = locked;
    }

    public void addRole(ClientRole role) {
        this.clientRole.add(role);
        role.setClientz(this);
    }

    public void addClientAddress(ClientAddress address) {
        this.clientAddress.add(address);
        address.setClientz(this);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this
                .clientRole
                .stream() //
                .map(role -> new SimpleGrantedAuthority(role.getRole().toString()))
                .collect(Collectors.toSet());
    }

}
