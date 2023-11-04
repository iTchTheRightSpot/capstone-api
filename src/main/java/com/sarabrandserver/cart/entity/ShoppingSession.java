package com.sarabrandserver.cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.TemporalType.TIMESTAMP;

@Table(name = "shopping_session")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ShoppingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", nullable = false, unique = true)
    private Long shoppingSessionId;

    @Column(name = "ip_address", nullable = false, unique = true, length = 39)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    @Temporal(TIMESTAMP)
    private Date createAt;

    @Column(name = "expire_at")
    @Temporal(TIMESTAMP)
    private Date expireAt;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "shoppingSession", orphanRemoval = true)
    private Set<CartItem> cartItems;

}
