package com.sarabrandserver.cart.entity;

import com.sarabrandserver.product.entity.ProductSku;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    private ShoppingSession shoppingSession;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "cartItem")
    private Set<ProductSku> skus;

    public CartItem(int qty, ShoppingSession shoppingSession, Set<ProductSku> skus) {
        this.qty = qty;
        this.shoppingSession = shoppingSession;
        this.skus = skus;
    }

    /** Persists  */
    public void addToCart(ProductSku sku) {
        this.skus.add(sku);
    }

}
