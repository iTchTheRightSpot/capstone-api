package com.sarabrandserver.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Table(name = "cart_item")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id", nullable = false, unique = true)
    private Long cartId;

    @Column(nullable = false)
    private int qty;

    @Column(nullable = false, length = 36)
    private String sku;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "session_id", nullable = false)
    private ShoppingSession shoppingSession;

    public CartItem(int qty, String sku, ShoppingSession shoppingSession) {
        this.qty = qty;
        this.sku = sku;
        this.shoppingSession = shoppingSession;
    }

}
