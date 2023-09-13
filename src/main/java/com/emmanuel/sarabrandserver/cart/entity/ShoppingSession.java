package com.emmanuel.sarabrandserver.cart.entity;

import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.user.entity.SarreBrandUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "shopping_session")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ShoppingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", nullable = false, unique = true)
    private Long shoppingSessionId;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    private SarreBrandUser sarreBrandUser;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "shoppingSession")
    private Set<Product> products;

}
