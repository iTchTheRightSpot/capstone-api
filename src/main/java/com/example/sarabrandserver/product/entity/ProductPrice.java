package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

@Table(name = "product_price")
@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ProductPrice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_price_id", nullable = false, unique = true)
    private Long productPriceId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "product_detail_id", referencedColumnName = "product_detail_id")
    private ProductDetail productDetail;

    @OneToMany(cascade = CascadeType.ALL, fetch = EAGER, mappedBy = "productPrice", orphanRemoval = true)
    private Set<CurrencyEntity> currencies = new HashSet<>();

    public void addCurrency(CurrencyEntity currencyEntity) {
        this.currencies.add(currencyEntity);
        currencyEntity.setProductPrice(this);
    }

}
