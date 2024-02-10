package com.sarabrandserver.cart.entity;

import com.sarabrandserver.payment.entity.OrderReservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.TemporalType.TIMESTAMP;

@Table(name = "shopping_session")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ShoppingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", nullable = false, unique = true)
    private Long shoppingSessionId;

    @Column(name = "cookie", nullable = false, unique = true, length = 39)
    private String cookie;

    @Column(name = "created_at", nullable = false)
    @Temporal(TIMESTAMP)
    private Date createAt;

    @Column(name = "expire_at")
    @Temporal(TIMESTAMP)
    private Date expireAt;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "shoppingSession", orphanRemoval = true)
    private Set<CartItem> cartItems;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "shoppingSession")
    private Set<OrderReservation> reservations;

    public ShoppingSession(
            String cookie,
            Date createAt,
            Date expireAt,
            Set<CartItem> cartItems,
            Set<OrderReservation> reservations
    ) {
        this.cookie = cookie;
        this.createAt = createAt;
        this.expireAt = expireAt;
        this.cartItems = cartItems;
        this.reservations = reservations;
    }

    public Long shoppingSessionId() {
        return shoppingSessionId;
    }

    public String cookie() {
        return cookie;
    }

    public Date createAt() {
        return createAt;
    }

    public Date expireAt() {
        return expireAt;
    }

    public Set<CartItem> cartItems() {
        return cartItems;
    }

    public Set<OrderReservation> reservations() {
        return reservations;
    }

}
