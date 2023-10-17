package com.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;

@Table(name = "price_currency")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PriceCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_currency_id", nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, length = 10)
    private String currency;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

}
