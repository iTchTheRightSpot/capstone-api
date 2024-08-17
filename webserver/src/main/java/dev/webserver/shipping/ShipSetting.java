package dev.webserver.shipping;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "ship_setting")
@Builder
public record ShipSetting(
        @Id
        @Column("ship_id")
        Long shipId,
        @NotNull(message = "ship_setting country cannot be null")
        @NotEmpty(message = "ship_setting country cannot be empty")
        @Size.List({@Size(max = 57, message = "ship_setting country length of 57")})
        String country,
        @NotNull(message = "ship_setting ngn_price cannot be null")
        @Column("ngn_price")
        BigDecimal ngnPrice,
        @NotNull(message = "ship_setting usd_price cannot be null")
        @Column("usd_price")
        BigDecimal usdPrice
) {
}
