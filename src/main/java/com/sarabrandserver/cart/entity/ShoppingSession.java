package com.sarabrandserver.cart.entity;

import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.user.entity.SarreBrandUser;
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

    @Column(nullable = false)
    private int qty;

    @Column(name = "created_at", nullable = false)
    @Temporal(TIMESTAMP)
    private Date createAt;

    @Column(name = "expire_at")
    @Temporal(TIMESTAMP)
    private Date expireAt;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    private SarreBrandUser sarreBrandUser;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "shoppingSession")
    private Set<ProductSku> skus;

    /**
     * Adds ProductSKU to users session
     */
    public void persist(ProductSku sku) {
        this.skus.add(sku);
    }

}
