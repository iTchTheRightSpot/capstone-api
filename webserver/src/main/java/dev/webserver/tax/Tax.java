package dev.webserver.tax;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "tax")
@Builder
public record Tax(
        @Id
        @Column("tax_id")
        Long taxId,
        @NotNull(message = "tax name be null")
        @NotEmpty(message = "tax name be empty")
        @Size.List({@Size(max = 5, message = "tax name length of 5")})
        String name,
        @NotNull(message = "tax rate be null")
        Double rate
) {
}
