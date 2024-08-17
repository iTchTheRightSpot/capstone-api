package dev.webserver.product;

import dev.webserver.enumeration.CapstoneCurrency;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "product_price_currency")
@Builder
public record ProductPriceCurrency(
        @Id
        @Column("currency_id")
        Long currencyId,
        @NotNull(message = "product_price_currency price cannot be null")
        BigDecimal price,
        CapstoneCurrency currency,
        @NotNull(message = "product_price_currency product_id cannot be null")
        @Column("product_id")
        Long productId
) {
}
