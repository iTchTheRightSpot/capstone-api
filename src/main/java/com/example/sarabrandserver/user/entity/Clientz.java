package com.example.sarabrandserver.user.entity;

import com.example.sarabrandserver.cart.entity.ShoppingSession;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "clientz")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Data
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

    @Column(name = "username", nullable = false, unique = true)
    private String username;

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

    @Column(name = "account_none_locked", nullable = false)
    private boolean accountNoneLocked;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "reset_id", referencedColumnName = "reset_id")
    private ClientPasswordResetToken token;

    @OneToOne(mappedBy = "clientz")
    private ShoppingSession shoppingSession;

    @OneToMany(cascade = {PERSIST, MERGE, REMOVE}, fetch = EAGER, mappedBy = "clientz", orphanRemoval = true)
    private Set<ClientRole> clientRole = new HashSet<>();

    public Clientz(
            String firstname,
            String lastname,
            String email,
            String username,
            String phoneNumber,
            String password,
            boolean enabled,
            boolean credentialsNonExpired,
            boolean accountNonExpired,
            boolean accountNoneLocked
    ) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.enabled = enabled;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonExpired = accountNonExpired;
        this.accountNoneLocked = accountNoneLocked;
    }

    public void addRole(ClientRole role) {
        this.clientRole.add(role);
        role.setClientz(this);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this
                .clientRole
                .stream() //
                .map(role -> new SimpleGrantedAuthority(role.getRole().toString()))
                .collect(Collectors.toSet());
    }

}
