package dev.webserver.tax;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "tax_setting")
@Builder
public record Tax(
        @Id
        @Column("tax_id")
        Long taxId,
        String name,
        Double rate
) {
}
