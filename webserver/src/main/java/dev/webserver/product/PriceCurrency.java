package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "price_currency")
@Builder
public record PriceCurrency(
        @Id
        @Column("price_currency_id")
        Long currencyId,
        BigDecimal price,
        SarreCurrency currency,
        @Column("product_id")
        Product productId
) {
}
