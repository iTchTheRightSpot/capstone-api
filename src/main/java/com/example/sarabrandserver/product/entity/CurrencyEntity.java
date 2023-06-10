package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "currency_entity")
@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CurrencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "currency_id", nullable = false, unique = true)
    private Long currencyId;

    @Column(name = "currency", nullable = false, length = 25)
    private String currency;

    @ManyToOne
    @JoinColumn(name = "product_price_id", referencedColumnName = "product_price_id")
    private ProductPrice productPrice;

    public CurrencyEntity(String currency) {
        this.currency = currency;
    }

}
