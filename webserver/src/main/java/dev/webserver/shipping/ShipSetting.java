package dev.webserver.shipping;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

// should have a relationship with price_currency
@Table(name = "ship_setting")
@Builder
public record ShipSetting(
        @Id
        @Column("ship_id")
        Long shipId,
        String country,
        @Column("ngn_price")
        BigDecimal ngnPrice,
        @Column("usd_price")
        BigDecimal usdPrice
) {
}
