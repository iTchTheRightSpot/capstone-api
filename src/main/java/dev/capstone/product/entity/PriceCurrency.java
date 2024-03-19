<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/entity/PriceCurrency.java
package dev.webserver.product.entity;

import dev.webserver.enumeration.SarreCurrency;
========
package dev.capstone.product.entity;

import dev.capstone.enumeration.SarreCurrency;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/entity/PriceCurrency.java
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Table(name = "price_currency")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class PriceCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_currency_id", nullable = false, unique = true)
    private Long currencyId;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SarreCurrency currency;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

    public PriceCurrency(BigDecimal price, SarreCurrency currency, Product product) {
        this.price = price;
        this.currency = currency;
        this.product = product;
    }

}
